package se.allco.githubbrowser.common.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.pow
import kotlin.math.roundToLong

operator fun CompositeDisposable.plusAssign(disposable: Disposable?) {
    add(disposable ?: return)
}

@Suppress("unused")
fun <T> Single<T>.doFinallyWithLastItem(action: (T?) -> Unit): Single<T> {
    return compose { upstream ->
        var lastItem: T? = null
        upstream
            .doOnSuccess { lastItem = it }
            .doFinally { action.invoke(lastItem) }
    }
}

fun <T> Observable<T>.addCounter(): Observable<Pair<T, Long>> =
    scan(Optional.none<T>() to AtomicLong(0)) { pair: Pair<Optional<T>, AtomicLong>, value: T ->
        value.asOptional() to pair.second
    }
        .skip(1)
        .map { it.first.asNullable()!! to it.second.getAndIncrement() }

@Suppress("unused")
class MaybeDroppableCache<T>(private val creator: () -> Maybe<T>) {

    private val trigger = BehaviorSubject.create<String>().also {
        it.onNext("initial_value")
    }

    private val shared = trigger
        .map { creator.invoke().cache() }
        .replay(1)
        .autoConnect()

    fun getDataStream(): Maybe<T> = shared
        .firstElement()
        .flatMap { it }

    fun dropCache() {
        trigger.onNext("drop_cache")
    }
}

fun <T> Maybe<T>.toSingleOptional(): Single<Optional<T>> =
    map { Optional.of(it) }.toSingle(Optional.None)

inline fun <reified T> Flowable<Optional<T>>.filterOptional(): Flowable<T> =
    ofType(Optional.Some::class.java).map { it.element as T }

inline fun <reified T> Observable<Optional<T>>.filterOptional(): Observable<T> =
    ofType(Optional.Some::class.java).map { it.element as T }

inline fun <reified T> Single<Optional<T>>.filterOptional(): Maybe<T> =
    toObservable().filterOptional().firstElement()

fun <T> ObservableEmitter<T>.onNextSafely(value: T?) {
    value?.takeIf { !isDisposed }?.also { onNext(it) }
}

fun <T> Observable<T>.attachCompletable(completable: Completable): Observable<T> =
    Observable.using<T, Disposable>(
        { completable.onErrorComplete().subscribeSafely() },
        { this },
        { it.dispose() }
    )

fun <T> Observable<T>.timeoutIfNotNull(
    timeoutMs: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    scheduler: Scheduler = Schedulers.io()
): Observable<T> =
    let { upstream ->
        timeoutMs.takeIf { it > 0 }
            ?.let { timeout: Long -> upstream.timeout(timeout, unit, scheduler) }
            ?: upstream
    }

fun <T> Single<T>.timeoutIfNotNull(
    timeoutMs: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    scheduler: Scheduler = Schedulers.io()
): Single<T> =
    let { upstream ->
        timeoutMs.takeIf { it > 0 }
            ?.let { timeout: Long -> upstream.timeout(timeout, unit, scheduler) }
            ?: upstream
    }

fun <T> Maybe<T>.timeoutIfNotNull(
    timeoutMs: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    scheduler: Scheduler = Schedulers.io()
): Maybe<T> =
    let { upstream ->
        timeoutMs.takeIf { it > 0 }
            ?.let { timeout: Long -> upstream.timeout(timeout, unit, scheduler) }
            ?: upstream
    }

fun Completable.timeoutIfNotNull(
    timeoutMs: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    scheduler: Scheduler = Schedulers.io()
): Completable =
    let { upstream ->
        timeoutMs.takeIf { it > 0 }
            ?.let { timeout: Long -> upstream.timeout(timeout, unit, scheduler) }
            ?: upstream
    }

@Suppress("unused")
fun <T> Observable<T>.delayIfNotNull(
    timeMs: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    scheduler: Scheduler = Schedulers.io()
): Observable<T> =
    let { upstream ->
        timeMs.takeIf { timeMs > 0 }?.let { upstream.delay(it, unit, scheduler) } ?: upstream
    }

/**
 * Ensures that onComplete does not pass through earlier than `timeoutMs` milliseconds
 */
@Suppress("UnstableApiUsage")
fun Completable.ensureCompletesAfter(timeoutMs: Long): Completable =
    toObservable<Any>()
        .materialize()
        .concatWith(Observable.never())
        .timeInterval()
        .concatMap { timed ->
            val diff = (timeoutMs - timed.time(TimeUnit.MILLISECONDS)).coerceAtLeast(0)
            Observable.just(timed.value()).delayIfNotNull(diff)
        }
        .dematerialize { it }
        .ignoreElements()

inline fun <reified T> Single<Optional<T>>.asSomeOrError(): Single<T> =
    map { it.asNullable() ?: throw Exception("Optional of ${T::class.java.simpleName} is None") }

fun <T, R> Single<Optional<T>>.mapOptional(block: (T) -> R?): Single<Optional<R>> =
    map { it.asNullable()?.let(block).asOptional() }

fun <T, R> Observable<Optional<T>>.mapOptional(block: (T) -> R?): Observable<Optional<R>> =
    map { it.asNullable()?.let(block).asOptional() }

fun <T, R> Observable<Optional<T>>.switchMapOptional(block: (T) -> Observable<Optional<R>>): Observable<Optional<R>> =
    switchMap {
        when (val value = it.asNullable()) {
            null -> Observable.just(Optional.None)
            else -> block(value)
        }
    }

fun <T, R> Single<Optional<T>>.switchMapOptional(block: (T) -> Single<Optional<R>>): Single<Optional<R>> =
    flatMap {
        when (val value = it.asNullable()) {
            null -> Single.just(Optional.None)
            else -> block(value)
        }
    }

/**
 * Do retries with [the exponential backoff algorithm](https://en.wikipedia.org/wiki/Exponential_backoff) to a stream.
 */
fun <T> Observable<T>.retryWithExponentialBackoff(
    initialRetryTime: Long = 5000L,
    retryTimeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    scheduler: Scheduler = Schedulers.io()
): Observable<T> {
    require(initialRetryTime >= 1) { "initial time $initialRetryTime must be > 0" }
    val initialRetryTimeMs = TimeUnit.MILLISECONDS.convert(initialRetryTime, retryTimeUnit)
    return this
        .retryWhen { errors ->
            errors
                .addCounter()
                .switchMapSingle { (_, counter) ->
                    val multiplier = 2.0.pow(counter + 1.0) - 1
                    val retryInMs = (initialRetryTimeMs * multiplier).roundToLong()
                    Single.timer(retryInMs, TimeUnit.MILLISECONDS, scheduler)
                }
                .observeOn(scheduler)
        }
}

fun Disposable.disposeWhenStopped(lifecycleOwner: LifecycleOwner) = disposeWhenStopped(lifecycleOwner.lifecycle)

fun Disposable.disposeWhenStopped(lifecycle: Lifecycle) {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            dispose()
            lifecycle.removeObserver(this)
        }
    })
}
