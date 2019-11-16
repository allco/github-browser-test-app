package se.allco.githubbrowser.app.ioc

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import se.allco.githubbrowser.app.BaseApplication
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

    fun inject(baseApplication: BaseApplication)
}