package se.allco.githubbrowser.app.di

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.common.logging.LoggingInterceptor

@Module

class NetworkModule {

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
    @JvmSuppressWildcards
    fun provideRetrofit(okHttpClientBuilder: OkHttpClient.Builder): (baseUrl: String) -> Retrofit =
        { baseUrl ->
            val gson = GsonBuilder()
                .setLenient()
                .create()

            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClientBuilder.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
}
