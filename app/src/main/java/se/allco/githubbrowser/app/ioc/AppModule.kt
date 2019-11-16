package se.allco.githubbrowser.app.ioc

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    fun providesApplicationContext(application: Application): Context = application.applicationContext
}
