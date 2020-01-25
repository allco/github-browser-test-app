package se.allco.githubbrowser.app.utils

import androidx.fragment.app.FragmentActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.disposables.SerialDisposable
import se.allco.githubbrowser.app.login.LoginActivity
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserRepository
import se.allco.githubbrowser.common.utils.attachLifecycleEventsObserver
import se.allco.githubbrowser.common.utils.subscribeSafely

fun FragmentActivity.ensureUserLoggedIn(userRepository: UserRepository, onValidUser: () -> Unit) {

    fun onUser(user: User, callback: (() -> Unit)? = null) {
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
                userRepository
                    .userFeed
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { onUser(it) }
                    .subscribeSafely()
            )
        }
        onPaused = {
            disposables.set(Disposables.empty())
        }
    }

    onUser(userRepository.getCurrentUser(), onValidUser)
}
