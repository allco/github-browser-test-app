package se.allco.githubbrowser.app._di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import se.allco.githubbrowser.app.BaseApplication
import se.allco.githubbrowser.app.login._di.LoginComponent
import se.allco.githubbrowser.app.user.UserComponentHolder
import se.allco.githubbrowser.app.user._di.UserComponent
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
