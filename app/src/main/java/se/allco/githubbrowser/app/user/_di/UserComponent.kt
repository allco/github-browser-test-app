package se.allco.githubbrowser.app.user._di

import dagger.BindsInstance
import dagger.Subcomponent
import se.allco.githubbrowser.app.main._di.MainComponent
import se.allco.githubbrowser.app.user.User
import javax.inject.Scope

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class UserScope

@UserScope
@Subcomponent
interface UserComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance user: User): UserComponent
    }

    fun createMainComponent(): MainComponent
}
