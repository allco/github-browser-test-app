package se.allco.githubbrowser.app.user

import android.webkit.CookieManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val tokenCache: TokenCache
) {

    companion object {
        private const val GET_USER_TIMEOUT_MS = 1_000L
        private const val LOGOUT_TIMEOUT_MS = 10_000L
    }

    private val userSubject = BehaviorSubject.createDefault<User>(User.Invalid).toSerialized()

    fun switchUser(user: User) {
        userSubject.onNext(user)
    }

    val userFeed: Observable<User> = userSubject

    fun getCurrentUser(): User =
        userSubject
            .timeout(GET_USER_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .blockingFirst()

    fun logoutUser(): Completable =
        userFeed
            .firstOrError()
            .flatMapCompletable { user ->
                when (user) {
                    is User.Valid -> doLogoutUser()
                    User.Invalid -> Completable.complete()
                }
            }

    private fun doLogoutUser() =
        tokenCache
            .erase()
            .andThen(removeAllCookies())
            .doOnComplete { switchUser(User.Invalid) }
            .timeout(LOGOUT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
}

private fun removeAllCookies() = Completable.create { emitter ->
    CookieManager.getInstance().removeAllCookies { emitter.onComplete() }
}

