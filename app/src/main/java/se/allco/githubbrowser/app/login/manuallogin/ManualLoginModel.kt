package se.allco.githubbrowser.app.login.manuallogin

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import se.allco.githubbrowser.app.user.User
import javax.inject.Inject

typealias GithubCode = String

class ManualLoginModel @Inject constructor(
    private val repository: ManualLoginRepository
) {

    fun processCode(code: String): Single<User.Valid> =
        repository
            .fetchAccessToken(code)
            .flatMap { token -> repository.fetchUserData(token) }
            .flatMap { user ->
                repository
                    .writeCachedToken(user.token)
                    .andThen(Single.just(user))
            }

    fun createErrorHandler(): Completable = repository.clearUserData()
}
