package se.allco.githubbrowser.app.main.account

import io.reactivex.Completable
import io.reactivex.Observable
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserModel
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val userModel: UserModel
) {

    fun getUserName(): Observable<String> =
        userModel
            .userFeed
            .ofType(User.Valid::class.java)
            .map { it.userName }

    fun logoutUser(): Completable = userModel.logoutUser()
}
