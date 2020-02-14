package se.allco.githubbrowser.app.user

import android.webkit.CookieManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserModel @Inject constructor(
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

    val userFeed: Observable<User> = userSubject.distinctUntilChanged()

    fun getCurrentUser(): User =
        userSubject
            .timeout(GET_USER_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .blockingFirst()

    fun logoutUser(): Completable =
        tokenCache
            .erase()
            .andThen(removeAllCookies())
            .doOnComplete { switchUser(User.Invalid) }
            .timeout(LOGOUT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
}

private fun removeAllCookies(): Completable =
    Completable.create { emitter ->
        CookieManager.getInstance().removeAllCookies { emitter.onComplete() }
    }.subscribeOn(AndroidSchedulers.mainThread())

