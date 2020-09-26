package se.allco.githubbrowser.common.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

class LifecycleObserver {
    var onPaused: (() -> Unit)? = null
    var onResumed: (() -> Unit)? = null
    var onStopped: (() -> Unit)? = null
    var onStarted: (() -> Unit)? = null
    var onCreated: (() -> Unit)? = null
    var onDestroyed: (() -> Unit)? = null
    var onEvent: ((Lifecycle.Event) -> Unit)? = null
}

/**
 * Significantly prettifies Lifecycle.Observer interface.
 * https://developer.android.com/topic/libraries/architecture/lifecycle#lc
 * calls `block.onStarted` when the lifecycle is under transition CREATED -> STARTED
 * calls `block.onStopped` when the lifecycle is under transition STARTED -> CREATED
 * calls `block.onDestroyed` when the lifecycle is under transition CREATED -> DESTROYED
 *
 * @return Disposable, which will be disposed when the lifecycle reaches DESTROYED state,
 * also it can be disposed by consumer then the lifecycle's observation will be stopped.
 *
 * Example:
 *      lifecycle.attachLifecycleEventsObserver{
 *          onStarted = {log("lifecycle reached STARTED state")}
 *          onStopped = {log("lifecycle has been just transitioned to CREATED state from STARTED state")}
 *      }
 */
fun Lifecycle.attachLifecycleEventsObserver(block: LifecycleObserver.() -> Unit): Disposable {
    val handler = LifecycleObserver().apply(block)
    val disposables = CompositeDisposable()
    val observer = object : DefaultLifecycleObserver {

        override fun onCreate(owner: LifecycleOwner) {
            handler.onCreated?.invoke()
            handler.onEvent?.invoke(Lifecycle.Event.ON_CREATE)
        }

        override fun onStart(owner: LifecycleOwner) {
            handler.onStarted?.invoke()
            handler.onEvent?.invoke(Lifecycle.Event.ON_START)
        }

        override fun onResume(owner: LifecycleOwner) {
            handler.onResumed?.invoke()
            handler.onEvent?.invoke(Lifecycle.Event.ON_RESUME)
        }

        override fun onPause(owner: LifecycleOwner) {
            handler.onPaused?.invoke()
            handler.onEvent?.invoke(Lifecycle.Event.ON_PAUSE)
        }

        override fun onStop(owner: LifecycleOwner) {
            handler.onStopped?.invoke()
            handler.onEvent?.invoke(Lifecycle.Event.ON_STOP)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            handler.onDestroyed?.invoke()
            handler.onEvent?.invoke(Lifecycle.Event.ON_DESTROY)
            disposables.dispose()
        }
    }
    disposables += Disposable.fromAction { removeObserver(observer) }
    addObserver(observer)
    return disposables
}

fun Lifecycle.getStates(): Observable<Lifecycle.State> =
    Observable
        .create { emitter: ObservableEmitter<Lifecycle.State> ->
            emitter.onNext(currentState)
            val disposable = attachLifecycleEventsObserver {
                onEvent = { emitter.onNext(currentState) }
            }
            emitter.setDisposable(disposable)
        }
        .takeUntil { it == Lifecycle.State.DESTROYED }
        .distinctUntilChanged()
