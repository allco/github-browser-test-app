package se.allco.githubbrowser.app.main.account

import io.reactivex.Completable
import io.reactivex.Observable
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserRepository
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val userRepository: UserRepository
) {

    fun getUserName(): Observable<String> =
        userRepository
            .userFeed
            .ofType(User.Valid::class.java)
            .map { it.userName }

    fun logoutUser(): Completable = userRepository.logoutUser()
}
