package se.allco.githubbrowser.app.di

import dagger.BindsInstance
import dagger.Subcomponent
import se.allco.githubbrowser.app.user.User

@Subcomponent
interface UserComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance user: User): UserComponent
    }
}
