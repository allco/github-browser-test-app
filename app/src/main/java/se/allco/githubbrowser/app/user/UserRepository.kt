package se.allco.githubbrowser.app.user

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {

    private val userSubject = BehaviorSubject.createDefault<User>(User.Invalid)

    fun switchUser(user: User) {
        userSubject.onNext(user)
    }

    fun logoutUser() {
        switchUser(User.Invalid)
    }

    val userFeed: Observable<User> = userSubject
}
