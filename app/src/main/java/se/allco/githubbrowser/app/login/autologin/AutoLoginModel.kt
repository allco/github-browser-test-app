package se.allco.githubbrowser.app.login.autologin

import io.reactivex.rxjava3.core.Single
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.utils.toSingleOptional
import javax.inject.Inject

class AutoLoginModel @Inject constructor(private val repository: AutoLoginRepository) {

    fun login(): Single<User> =
        repository
            .readCachedToken()
            .toSingleOptional()
            .flatMap { tokenOptional ->
                tokenOptional.asNullable()
                    ?.let { repository.fetchUserData(it) }
                    ?: Single.just(User.Invalid)
            }
            .onErrorResumeNext {
                repository
                    .clearUserData()
                    .andThen(Single.just(User.Invalid))
            }
}
