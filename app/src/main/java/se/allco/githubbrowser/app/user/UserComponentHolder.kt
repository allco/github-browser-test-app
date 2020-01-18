package se.allco.githubbrowser.app.user

import io.reactivex.subjects.BehaviorSubject
import se.allco.githubbrowser.app.di.AppComponent

class UserComponentHolder(
    private val appComponent: AppComponent
) {

    private val userComponent = BehaviorSubject.createDefault<User>(
        User.Invalid as User
    )

    fun setUser(user: User) {
        appComponent.createUserComponentFactory().create(user)
    }
}
