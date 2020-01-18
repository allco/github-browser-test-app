package se.allco.githubbrowser.app.login

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.utils.subscribeSafely
import timber.log.Timber
import javax.inject.Inject

/**
 * Implements business logic for the login procedure.
 * It tries to read the token from the local storage first, and
 * if it fails, it asks VM to launch UI for the manual login.
 */
class LoginModel @Inject constructor(
    private val repository: LoginRepository
) {

    sealed class Signal {
        data class PerformManualLogin(val callback: SingleSubject<GithubToken>) : Signal()
        data class Success(val user: User.Valid) : Signal()
    }

    private val manualLoginTokenEmitter = SingleSubject.create<GithubToken>()

    // Retrieves the last known gitHub token and then fetches the user info from GitHub.
    private val autoLogin: Maybe<User.Valid> =
        repository.readCachedToken().flatMap { token -> repository.fetchGithubUser(token).toMaybe() }

    // Waits for gitHubToken from [manualLoginTokenEmitter] and then fetches the user info from GitHub.
    private val manualLogin: Single<User.Valid> =
        manualLoginTokenEmitter.flatMap { token -> repository.fetchGithubUser(token) }

    /**
     * @return stream of [Signal] where:
     *  [Signal.PerformManualLogin] - means the user has to log in manually
     *      and the GitHub token needs to be delivered back with [Signal.RunManualLogin.callback].
     *  [Signal.Success] - means the user is logged in successfully.
     */
    fun loginUser(): Observable<Signal> = Observable.create<Signal> { emitter ->
        // It tries to:
        // 1. Perform the auto login, ignore all errors.
        // 2. Perform the manual login if the auto login is failed.
        // 3. Notify UserComponentHolder if the user is logged in.
        // 4. Save the user GitHub token to the storage.
        emitter.setDisposable(
            autoLogin
                .doOnError { Timber.w(it, "Auto login failed") }
                .onErrorComplete()
                .switchIfEmpty(
                    manualLogin.doOnSubscribe {
                        emitter.onNext(Signal.PerformManualLogin(manualLoginTokenEmitter))
                    }
                )
                .flatMapCompletable { user ->
                    repository.switchLoggedInUser(user)
                    repository
                        .writeCachedToken(user)
                        .doOnComplete { emitter.onNext(Signal.Success(user)) }
                }
                .doOnError(emitter::onError)
                .onErrorComplete()
                .subscribeSafely()
        )
    }
}

