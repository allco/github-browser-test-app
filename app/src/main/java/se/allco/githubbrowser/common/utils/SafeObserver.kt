package se.allco.githubbrowser.common.utils

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.CompletableObserver
import io.reactivex.Emitter
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.MaybeObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleObserver
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.internal.subscriptions.SubscriptionHelper
import io.reactivex.internal.util.EndConsumerHelper
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

private const val CALL_STACK_DEPTH = 2

private class SafeObserver<T>(
    val tag: String,
    val handler: OnEvenHandler<T>? = null
) :
    CompletableObserver,
    SingleObserver<T>,
    MaybeObserver<T>,
    Observer<T>,
    Disposable {
    private val refDisposable = AtomicReference<Disposable>()

    override fun onSubscribe(@NonNull s: Disposable) {
        EndConsumerHelper.setOnce(this.refDisposable, s, javaClass)
    }

    override fun isDisposed(): Boolean {
        return refDisposable.get() === DisposableHelper.DISPOSED
    }

    override fun dispose() {
        DisposableHelper.dispose(refDisposable)
    }

    override fun onError(err: Throwable) {
        Timber.w(err, "SafeObserver got OnError at $tag")
        handler?.onError?.invoke(err) ?: throw RuntimeException("Unhandled rx onError at $tag", err)
    }

    override fun onNext(t: T) {
        handler?.onNext?.invoke(t)
    }

    override fun onSuccess(t: T) {
        handler?.onSuccess?.invoke(t)
    }

    override fun onComplete() {
        handler?.onComplete?.invoke()
    }
}

private class EmptySubscriber<T>(
    val tag: String,
    val handler: OnEvenHandler<T>? = null
) :
    Subscriber<T>,
    Subscription,
    Disposable {

    companion object;

    private val refSubscription = AtomicReference<Subscription>()

    override fun onSubscribe(s: Subscription) {
        if (EndConsumerHelper.setOnce(refSubscription, s, javaClass)) {
            refSubscription.get().request(java.lang.Long.MAX_VALUE)
        }
    }

    override fun request(n: Long) {
        refSubscription.get().request(n)
    }

    override fun cancel() {
        SubscriptionHelper.cancel(refSubscription)
    }

    override fun dispose() = cancel()

    override fun isDisposed(): Boolean = refSubscription.get() === SubscriptionHelper.CANCELLED

    override fun onError(err: Throwable) {
        Timber.w(err, "EmptySubscriber got OnError at $tag")
        handler?.onError?.invoke(err)
    }

    override fun onComplete() {
        handler?.onComplete?.invoke()
    }

    override fun onNext(t: T) {
        handler?.onNext?.invoke(t)
    }
}

interface OnSingleHandler<T> {
    var onSuccess: ((T) -> Unit)?
    var onError: ((Throwable) -> Unit)?
}

interface OnObservableHandler<T> {
    var onNext: ((T) -> Unit)?
    var onError: ((Throwable) -> Unit)?
    var onComplete: (() -> Unit)?
}

interface OnCompletableHandler {
    var onError: ((Throwable) -> Unit)?
    var onComplete: (() -> Unit)?
}

interface OnMaybeHandler<T> {
    var onSuccess: ((T) -> Unit)?
    var onError: ((Throwable) -> Unit)?
    var onComplete: (() -> Unit)?
}

private class OnEvenHandler<T> : OnSingleHandler<T>, OnObservableHandler<T>, OnMaybeHandler<T>,
    OnCompletableHandler {
    override var onNext: ((T) -> Unit)? = null
    override var onSuccess: ((T) -> Unit)? = null
    override var onError: ((Throwable) -> Unit)? = null
    override var onComplete: (() -> Unit)? = null
}

fun Completable.subscribeSafely(action: OnCompletableHandler.() -> Unit): Disposable {
    return subscribeWith(
        SafeObserver(
            getCallPlace(CALL_STACK_DEPTH),
            OnEvenHandler<Any>().apply(action)
        )
    )
}

fun Completable.subscribeSafely(emitter: CompletableEmitter): Disposable {
    return subscribeWith(SafeObserver(getCallPlace(CALL_STACK_DEPTH), OnEvenHandler<Any>().apply {
        onComplete = emitter::onComplete
        onError = emitter::onError
    }))
}

fun <T> Observable<T>.subscribeSafely(observer: Observer<T>): Disposable {
    return subscribeWith(SafeObserver(getCallPlace(CALL_STACK_DEPTH), OnEvenHandler<T>().apply {
        onComplete = observer::onComplete
        onError = observer::onError
        onNext = observer::onNext
    }))
}

fun <T> Observable<T>.subscribeSafely(observer: Emitter<T>): Disposable {
    return subscribeWith(SafeObserver(getCallPlace(CALL_STACK_DEPTH), OnEvenHandler<T>().apply {
        onComplete = observer::onComplete
        onError = observer::onError
        onNext = observer::onNext
    }))
}

fun <T> Single<T>.subscribeSafely(observer: SingleEmitter<T>): Disposable {
    return subscribeWith(SafeObserver(getCallPlace(CALL_STACK_DEPTH), OnEvenHandler<T>().apply {
        onSuccess = observer::onSuccess
        onError = observer::onError
    }))
}

fun <T> Observable<T>.subscribeSafely(): Disposable {
    return subscribeWith(SafeObserver(getCallPlace(CALL_STACK_DEPTH)))
}

fun <T> Maybe<T>.subscribeSafely(): Disposable {
    return subscribeWith(SafeObserver(getCallPlace(CALL_STACK_DEPTH)))
}

fun <T> Single<T>.subscribeSafely(): Disposable {
    return subscribeWith(SafeObserver(getCallPlace(CALL_STACK_DEPTH)))
}

fun Completable.subscribeSafely(): Disposable {
    return subscribeWith(SafeObserver<Unit>(getCallPlace(CALL_STACK_DEPTH)))
}

fun <T> Flowable<T>.subscribeSafely(): Disposable {
    return subscribeWith(EmptySubscriber(getCallPlace(CALL_STACK_DEPTH)))
}

fun <T> Observable<T>.subscribeSafely(action: OnObservableHandler<T>.() -> Unit): Disposable {
    return subscribeWith(
        SafeObserver(
            getCallPlace(CALL_STACK_DEPTH),
            OnEvenHandler<T>().apply(action)
        )
    )
}

fun <T> Single<T>.subscribeSafely(action: OnSingleHandler<T>.() -> Unit): Disposable {
    return subscribeWith(
        SafeObserver(
            getCallPlace(CALL_STACK_DEPTH),
            OnEvenHandler<T>().apply(action)
        )
    )
}

fun <T> Maybe<T>.subscribeSafely(action: OnMaybeHandler<T>.() -> Unit): Disposable {
    return subscribeWith(
        SafeObserver(
            getCallPlace(CALL_STACK_DEPTH),
            OnEvenHandler<T>().apply(action)
        )
    )
}

fun <T> Flowable<T>.subscribeSafely(action: OnObservableHandler<T>.() -> Unit): Disposable {
    return subscribeWith(
        EmptySubscriber(
            getCallPlace(CALL_STACK_DEPTH),
            OnEvenHandler<T>().apply(action)
        )
    )
}
