package se.allco.githubbrowser.app.user

import android.content.Context
import android.webkit.CookieManager
import androidx.annotation.MainThread
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import se.allco.githubbrowser.app._di.AppComponent
import se.allco.githubbrowser.app.user._di.UserComponent
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserComponentHolder @Inject constructor(
    private val tokenCache: TokenCache,
    private val userComponentFactory: UserComponent.Factory
) {
    companion object {
        private const val LOGOUT_TIMEOUT_MS = 10_000L

        fun getInstance(context: Context): UserComponentHolder =
            AppComponent
                .getInstance(context)
                .getUserComponentHolder()

        fun getUserComponent(context: Context): UserComponent =
            getInstance(context).getUserComponent()
    }

    private val componentSubject =
        BehaviorSubject.createDefault(userComponentFactory.create(User.Invalid))

    fun getUserComponent(): UserComponent =
        requireNotNull(componentSubject.value) { "componentSubject should never have `null` as a value" }

    fun getUserComponentsFeed(): Observable<UserComponent> = componentSubject

    @MainThread
    fun switchUser(user: User) {
        componentSubject.onNext(userComponentFactory.create(user))
    }

    fun logoutUser(): Completable =
        tokenCache
            .erase()
            .andThen(removeAllCookies())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { switchUser(User.Invalid) }
            .timeout(LOGOUT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
}

private fun removeAllCookies(): Completable =
    Completable.create { emitter ->
        CookieManager.getInstance().removeAllCookies { emitter.onComplete() }
    }.subscribeOn(AndroidSchedulers.mainThread())
