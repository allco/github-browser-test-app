package se.allco.githubbrowser.app.user

import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface UserComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance user: User): UserComponent
    }
}
