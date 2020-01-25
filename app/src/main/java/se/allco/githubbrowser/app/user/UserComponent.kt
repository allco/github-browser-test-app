package se.allco.githubbrowser.app.user

import dagger.BindsInstance
import dagger.Subcomponent
import se.allco.githubbrowser.app.main.di.MainComponent

@Subcomponent
interface UserComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance user: User): UserComponent
    }

    fun createMainComponent(): MainComponent
}
