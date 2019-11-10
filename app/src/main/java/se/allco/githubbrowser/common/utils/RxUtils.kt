package se.allco.githubbrowser.common.utils

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

operator fun CompositeDisposable.plusAssign(disposable: Disposable?) {
    add(disposable ?: return)
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
