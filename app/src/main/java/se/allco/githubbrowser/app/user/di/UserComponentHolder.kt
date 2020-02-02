package se.allco.githubbrowser.app.user.di

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.disposables.SerialDisposable
import se.allco.githubbrowser.app.di.AppComponent
import se.allco.githubbrowser.app.di.AppModule
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserModel
import se.allco.githubbrowser.common.utils.attachLifecycleEventsObserver
import se.allco.githubbrowser.common.utils.subscribeSafely
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserComponentHolder @Inject constructor(
    private val userModel: UserModel,
    private val userComponentFactory: UserComponent.Factory,
    @Named(AppModule.PROCESS_LIFECYCLE_OWNER) processLifecycleOwner: LifecycleOwner
) {
    companion object {
        fun getUserComponent(context: Context) =
            AppComponent
                .getInstance(context)
                .getUserComponentHolder()
                .getUserComponent()
    }

    init {
        val disposables = SerialDisposable()
        processLifecycleOwner.lifecycle.attachLifecycleEventsObserver {
            onStarted = {
                disposables.set(
                    userModel
                        .userFeed
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(::onUserChanged)
                        .subscribeSafely()
                )
            }

            onStopped = {
                disposables.set(Disposables.empty())
            }
        }
    }

    private var userComponent: UserComponent =
        userComponentFactory.create(User.Invalid)

    private fun onUserChanged(user: User) {
        userComponent = userComponentFactory.create(user)
    }

    fun getUserComponent(): UserComponent = userComponent
}
