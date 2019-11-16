package se.allco.githubbrowser.app.ioc

import android.app.Application
import android.content.Context
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
}
