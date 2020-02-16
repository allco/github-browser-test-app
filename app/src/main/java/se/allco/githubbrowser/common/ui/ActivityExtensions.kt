package se.allco.githubbrowser.common.ui

import androidx.fragment.app.FragmentActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.disposables.SerialDisposable
import se.allco.githubbrowser.app.login.LoginActivity
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserComponentHolder
import se.allco.githubbrowser.common.utils.attachLifecycleEventsObserver
import se.allco.githubbrowser.common.utils.subscribeSafely

fun FragmentActivity.ensureUserLoggedIn(onValidUser: () -> Unit) {

    fun onUserChanged(user: User, callback: (() -> Unit)? = null) {
        when (user) {
            is User.Valid -> callback?.invoke()
            else -> {
                startActivity(LoginActivity.createIntent(this))
                finishAfterTransition()
            }
        }
    }

    val disposables = SerialDisposable()
    lifecycle.attachLifecycleEventsObserver {
        onResumed = {
            disposables.set(
                UserComponentHolder
                    .getInstance(this@ensureUserLoggedIn)
                    .getUserComponentsFeed()
                    .map { it.getCurrentUser() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { onUserChanged(it) }
                    .subscribeSafely()
            )
        }
        onPaused = {
            disposables.set(Disposables.empty())
        }
    }

    onUserChanged(
        UserComponentHolder
            .getUserComponent(this)
            .getCurrentUser(),
        onValidUser
    )
}

