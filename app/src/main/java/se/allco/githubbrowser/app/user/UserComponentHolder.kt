package se.allco.githubbrowser.app.user

import androidx.lifecycle.ProcessLifecycleOwner
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.disposables.SerialDisposable
import se.allco.githubbrowser.app.di.AppComponent
import se.allco.githubbrowser.app.di.AppModule
import se.allco.githubbrowser.common.utils.attachLifecycleEventsObserver
import se.allco.githubbrowser.common.utils.subscribeSafely
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserComponentHolder(
    private val appComponent: AppComponent,
    private val userRepository: UserRepository,
    @Named(AppModule.PROCESS_LIFECYCLE_OWNER) processLifecycleOwner: ProcessLifecycleOwner
) {
    init {
        val disposables = SerialDisposable()
        processLifecycleOwner.lifecycle.attachLifecycleEventsObserver {
            onStarted = {
                disposables.set(
                    userRepository
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
        appComponent.getUserComponentFactory().create(User.Invalid)

    private fun onUserChanged(user: User) {
        userComponent = appComponent.getUserComponentFactory().create(user)
    }

    fun getUserComponent(): UserComponent = userComponent
}
