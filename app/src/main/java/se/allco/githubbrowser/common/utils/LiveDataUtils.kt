package se.allco.githubbrowser.common.utils

import androidx.core.app.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import timber.log.Timber

@Suppress("FunctionName")
fun <T> ObserverNonNull(observer: (T) -> Unit): Observer<T> = Observer { value -> value?.apply(observer) }

fun <T : Any> Fragment.observe(liveData: LiveData<T>): Pair<LifecycleOwner, LiveData<T>> =
    Pair(viewLifecycleOwner, liveData)

fun <T : Any> ComponentActivity.observe(liveData: LiveData<T>): Pair<LifecycleOwner, LiveData<T>> =
    Pair(this as LifecycleOwner, liveData)

/**
 * Attaches [LiveData] to a [LifecycleOwner] without a callback.
 * It is useful for subscribing to a lifecycle without necessarily attaching it to a view or being interested in any callback when the work is done fully by the [LiveData].
 */
fun <T> LiveData<T>.attachTo(lifecycleOwner: LifecycleOwner) {
    observe(lifecycleOwner, Observer {})
}

infix fun <T : Any> Pair<LifecycleOwner, LiveData<T>>.with(handler: (value: T) -> Unit) {
    second.observe(first, ObserverNonNull { value: T -> value.apply(handler) })
}

fun <T> MutableLiveData<T>.postValueIfChanged(value: T) {
    if (this.value != value) this.postValue(value)
}

/**
 * Similar to Observable.combineLatest(...)
 */
fun <T1, T2, T3> LiveData<T1>.combine(
    source: LiveData<T2>,
    initialValue: T3?,
    action: (T1?, T2?) -> T3?
): LiveData<T3> {
    check(!(source === this)) { "`source` is the same LiveData as the receiver" }
    var left: Optional<T1?> = Optional.None
    var right: Optional<T2?> = Optional.None
    val mediator = MediatorLiveData<T3>()
    mediator.postValue(initialValue)
    fun checkAndPostData() {
        if (left != Optional.None && right != Optional.None) {
            mediator.postValue(action(left.asNullable(), right.asNullable()))
        }
    }
    mediator.addSource(this) { t: T1? ->
        left = Optional.of(t)
        checkAndPostData()
    }
    mediator.addSource(source) { t: T2? ->
        right = Optional.of(t)
        checkAndPostData()
    }
    return mediator
}

fun <T1, T2, T3> combineLiveData(
    left: LiveData<T1>,
    right: LiveData<T2>,
    initialValue: T3?,
    action: (T1?, T2?) -> T3?
): LiveData<T3> =
    left.combine(right, initialValue, action)

private fun <T> Flowable<T>.toLiveDataImpl(callStackDepth: Int): LiveData<T> {
    val callPlace = getCallPlace(callStackDepth = callStackDepth)
    return LiveDataReactiveStreams.fromPublisher(
        onErrorResumeNext { err: Throwable ->
            Timber.w(err, "Rx stream `toLiveData()` got OnError at $callPlace")
            Flowable.empty()
        }
    )
}

private fun <T> Observable<T>.toLiveDataImpl(callStackDepth: Int): LiveData<T> =
    toFlowable(BackpressureStrategy.LATEST).toLiveDataImpl(callStackDepth)

private fun <T> Single<T>.toLiveDataImpl(callStackDepth: Int): LiveData<T> =
    toFlowable().toLiveDataImpl(callStackDepth)

/**
 * Converts Rx stream to LiveData.
 * onError event from the Rx stream will be reported to logcat and swallowed.
 * The source Rx stream will be subscribed and disposed when the returned LiveData instance
 * is observed and unobserved respectively.
 */
fun <T> Flowable<T>.toLiveData(): LiveData<T> = toLiveDataImpl(callStackDepth = 2)

/**
 * Converts Rx stream to LiveData.
 * onError event from the Rx stream will be reported to logcat and swallowed.
 * The source Rx stream will be subscribed and disposed when the returned LiveData instance
 * is observed and unobserved respectively.
 */
fun <T> Observable<T>.toLiveData(): LiveData<T> = toLiveDataImpl(callStackDepth = 2)

/**
 * Converts Rx stream to LiveData.
 * onError event from the Rx stream will be reported to logcat and swallowed.
 * otherwise RuntimeException: "LiveData does not handle errors" will be thrown.
 */
fun <T> Single<T>.toLiveData(): LiveData<T> = toLiveDataImpl(callStackDepth = 2)

/**
 * Converts Rx stream to LiveData.
 * onError event from the Rx stream will be reported to logcat and swallowed.
 * The source Rx stream will be subscribed immediately
 * and disposed only when the disposable added to `disposableContainer` is disposed.
 */
fun <T> Observable<T>.toLiveData(disposableContainer: CompositeDisposable): LiveData<T> =
    toLiveDataImpl(callStackDepth = 3).apply {
        if (!disposableContainer.isDisposed) {
            val dummyObserver = Observer<T> {}
            observeForever(dummyObserver)
            disposableContainer.add(Disposables.fromAction { removeObserver(dummyObserver) })
        }
    }

fun <T> Single<T>.toLiveData(disposableContainer: CompositeDisposable): LiveData<T> =
    toObservable().toLiveData(disposableContainer)

/**
 * Creates a LiveData with initial value
 * @param initialValue the first value to be emitted
 * @param thenLiveData LiveData that should be appended
 */
fun <T> startWithAndThen(initialValue: T?, thenLiveData: LiveData<T>): LiveData<T> =
    MediatorLiveData<T>().apply {
        addSource(thenLiveData) { value = it }
        value = initialValue
    }

/**
 * Converts LiveData to a Rx stream.
 * When LiveData emits `null` it is being converted to Optional.None.
 */
fun <T> LiveData<T>.toObservable(): Observable<Optional<T>> = Observable.create { emitter ->
    val observer: (T?) -> Unit = {
        when (it) {
            null -> emitter.onNextSafely(Optional.None)
            else -> emitter.onNextSafely(Optional.of(it))
        }
    }
    observeForever(observer)
    emitter.setCancellable { removeObserver(observer) }
}

fun <T> LiveData<T>.mergeWith(liveData: LiveData<T>): LiveData<T> =
    MediatorLiveData<T>().apply {
        addSource(this) { value = it }
        addSource(liveData) { value = it }
    }

/**
 * @see Transformations.map
 */
fun <T, R> LiveData<T>.mapNotNull(func: (T) -> R?): LiveData<R> =
    Transformations.map(this) { value: T? -> value?.let(func) }

fun <T, R> LiveData<T>.map(func: (T?) -> R?): LiveData<R> =
    Transformations.map(this, func)

/**
 * @see Transformations.distinctUntilChanged
 */
fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> =
    Transformations.distinctUntilChanged(this)

/**
 * @see Transformations.switchMap
 */
fun <T, R> LiveData<T>.switchMap(func: (T) -> LiveData<R>): LiveData<R> {
    return Transformations.switchMap(this, func)
}
