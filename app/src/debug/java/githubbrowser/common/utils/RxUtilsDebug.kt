package githubbrowser.common.utils

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber

internal fun log(value: Any) {
    log(null, value)
}

internal fun log(tag: Any?, value: Any) {
    val thread = Thread.currentThread()
    Timber.v("${tag?.let { "{$it}" } ?: ""} $value thread[${thread.name}(${thread.id})]")
}

internal fun log(err: Throwable) {
    Timber.v("error[${err.message}] thread:${Thread.currentThread()}")
}

fun <T> Observable<T>.dump(tag: String): Observable<T> =
    this
        .doFinally { log(tag, "doFinally") }
        .doOnNext { log(tag, "onNext: ${it.toString()}") }
        .doOnError { er -> log(tag, "onError: $er") }
        .doOnSubscribe { log(tag, "onSubscribed") }
        .doOnDispose { log(tag, "onDisposed") }
        .doOnComplete { log(tag, "onComplete") }

fun <T> Flowable<T>.dump(tag: String): Flowable<T> =
    this
        .doFinally { log(tag, "doFinally") }
        .doOnNext { log(tag, "onNext: ${it.toString()}") }
        .doOnError { er -> log(tag, "onError: $er") }
        .doOnComplete { log(tag, "onComplete") }
        .doOnSubscribe { log(tag, "onSubscribed") }
        .doOnCancel { log(tag, "doCanceled") }
        .doOnRequest { req -> log(tag, "onRequested $req") }
        .doOnCancel { log(tag, "onCanceled") }

fun <T> Single<T>.dump(tag: String): Single<T> =
    this
        .doFinally { log(tag, "doFinally") }
        .doOnSuccess { log(tag, "onSuccess: ${it.toString()}") }
        .doOnError { er -> log(tag, "onError: $er") }
        .doOnSubscribe { log(tag, "onSubscribed") }
        .doOnDispose { log(tag, "onDisposed") }

fun <T> Maybe<T>.dump(tag: String): Maybe<T> =
    this
        .doFinally { log(tag, "doFinally") }
        .doOnSuccess { log(tag, "onSuccess: ${it.toString()}") }
        .doOnComplete { log(tag, "onComplete") }
        .doOnError { er -> log(tag, "onError: $er") }
        .doOnSubscribe { log(tag, "onSubscribed") }
        .doOnDispose { log(tag, "onDisposed") }

fun Completable.dump(tag: String): Completable =
    this
        .doFinally { log(tag, "doFinally") }
        .doOnComplete { log(tag, "onComplete") }
        .doOnError { er -> log(tag, "onError: $er") }
        .doOnSubscribe { log(tag, "onSubscribed") }
        .doOnDispose { log(tag, "onDisposed") }
