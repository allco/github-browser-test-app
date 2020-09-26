package se.allco.githubbrowser.app._di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.common.NetworkReporter
import se.allco.githubbrowser.common.NetworkReporterImpl
import se.allco.githubbrowser.common.logging.LoggingInterceptor
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule {

    companion object {
        const val PROCESS_LIFECYCLE_OWNER = "PROCESS_LIFECYCLE_OWNER"
    }

    @Provides
    fun providesApplicationContext(application: Application): Context =
        application.applicationContext

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

    @Provides
    fun provideOkHttpClientBuilder(loggingInterceptorBuilder: LoggingInterceptor.Builder): OkHttpClient.Builder =
        OkHttpClient
            .Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(loggingInterceptorBuilder.build())
                }
            }

    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder =
        Retrofit
            .Builder()
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient()
                        .create()
                )
            )
}
