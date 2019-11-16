package se.allco.githubbrowser.app

import android.app.Application
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.app.ioc.AppComponent
import se.allco.githubbrowser.app.ioc.DaggerAppComponent
import timber.log.Timber

class BaseApplication : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        if (isDebugBuild()) {
            Timber.plant(Timber.DebugTree())
        }

        appComponent = createAppComponent()
        appComponent.inject(this)
    }

    private fun createAppComponent(): AppComponent =
        appComponentCreator
            ?.invoke()
            ?: DaggerAppComponent.builder().apply { setApplication(this@BaseApplication) }.build()

    companion object {
        // All the point of this field is to allow to hook the creation of the AppComponent.
        // It can be set from any test and then the original(production) version will be ignored.
        var appComponentCreator: (() -> AppComponent)? = null

        fun isDebugBuild(): Boolean = BuildConfig.DEBUG
    }
}
