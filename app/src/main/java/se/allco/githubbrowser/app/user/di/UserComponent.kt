package se.allco.githubbrowser.app.user.di

import dagger.BindsInstance
import dagger.Subcomponent
import se.allco.githubbrowser.app.main.di.MainComponent
import se.allco.githubbrowser.app.user.User

@Subcomponent(modules = [UserModule::class])
interface UserComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance user: User): UserComponent
    }

    fun createMainComponent(): MainComponent
}
