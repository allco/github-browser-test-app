package se.allco.githubbrowser.app.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import se.allco.githubbrowser.common.NetworkReporter
import se.allco.githubbrowser.common.NetworkReporterImpl
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule {

    companion object {
        const val PROCESS_LIFECYCLE_OWNER = "PROCESS_LIFECYCLE_OWNER"
    }

    @Provides
    fun providesApplicationContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun providesNetworkReporter(impl: NetworkReporterImpl): NetworkReporter = impl

    @Provides
    fun provideGsonBuilder() = GsonBuilder()

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences("settings", Activity.MODE_PRIVATE)!!

    @Provides
    @Named(PROCESS_LIFECYCLE_OWNER)
    fun getProcessLifeCycle(): LifecycleOwner = ProcessLifecycleOwner.get()
}
