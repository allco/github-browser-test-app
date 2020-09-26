package se.allco.githubbrowser.app.main.account

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserComponentHolder
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val userComponentHolder: UserComponentHolder
) {
    data class Account(val name: String, val imageUrl: String?)

    fun getAccount(): Observable<Account> =
        userComponentHolder
            .getUserComponentsFeed()
            .map { it.getCurrentUser() }
            .map { requireNotNull(it as? User.Valid).asAccount() }

    fun logoutUser(): Completable = userComponentHolder.logoutUser()
}

fun User.Valid.asAccount() =
    AccountRepository.Account(
        name = this.userName,
        imageUrl = imageUrl
    )

