package se.allco.githubbrowser.app.main.account

import io.reactivex.Completable
import io.reactivex.Observable
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserComponentHolder
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val userComponentHolder: UserComponentHolder
) {

    fun getUserName(): Observable<String> =
        userComponentHolder
            .getUserComponentsFeed()
            .map { it.getCurrentUser() }
            .map { requireNotNull(it as? User.Valid).userName }

    fun logoutUser(): Completable = userComponentHolder.logoutUser()
}
