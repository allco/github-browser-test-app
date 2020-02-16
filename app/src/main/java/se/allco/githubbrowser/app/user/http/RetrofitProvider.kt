package se.allco.githubbrowser.app.user.http

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import se.allco.githubbrowser.BuildConfig
import javax.inject.Inject
import javax.inject.Provider

class RetrofitProvider @Inject constructor(
    private val authInterceptor: AuthenticationInterceptor,
    private val okHttpBuilder: OkHttpClient.Builder,
    private val retrofitBuilder: Retrofit.Builder
) : Provider<Retrofit> {

    override fun get(): Retrofit =
        retrofitBuilder
            .client(
                okHttpBuilder
                    .addInterceptor(authInterceptor)
                    .build()
            )
            .baseUrl(BuildConfig.GITHUB_API_BASE_URL)
            .build()
}
