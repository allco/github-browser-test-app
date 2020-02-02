package se.allco.githubbrowser.app.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import se.allco.githubbrowser.app.BaseApplication
import se.allco.githubbrowser.app.login.di.LoginComponent
import se.allco.githubbrowser.app.user.di.UserComponent
import se.allco.githubbrowser.app.user.di.UserComponentHolder
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    companion object {
        fun getInstance(context: Context): AppComponent =
            (context.applicationContext as BaseApplication).appComponent
    }

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun setApplication(application: Application)
        fun build(): AppComponent
    }

    fun createLoginComponent(): LoginComponent
    fun getUserComponentHolder(): UserComponentHolder
    fun getUserComponentFactory(): UserComponent.Factory
    fun inject(baseApplication: BaseApplication)
}
