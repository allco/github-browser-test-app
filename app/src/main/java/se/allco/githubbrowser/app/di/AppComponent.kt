package se.allco.githubbrowser.app.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import se.allco.githubbrowser.app.BaseApplication
import se.allco.githubbrowser.app.login.LoginActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, NetworkModule::class])
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

    fun getUserComponentFactory(): UserComponent.Factory

    fun inject(baseApplication: BaseApplication)
    fun inject(activity: LoginActivity)
}
