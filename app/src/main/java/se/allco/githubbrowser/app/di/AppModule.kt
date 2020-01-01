package se.allco.githubbrowser.app.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import se.allco.githubbrowser.common.NetworkReporter
import se.allco.githubbrowser.common.NetworkReporterImpl
import javax.inject.Singleton

@Module
class AppModule {

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
}
