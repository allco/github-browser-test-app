package se.allco.githubbrowser.app.user.http

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.app.user._di.UserScope
import javax.inject.Inject

@UserScope
class RetrofitFactory @Inject constructor(
    private val authInterceptor: AuthenticationInterceptor,
    private val okHttpBuilder: OkHttpClient.Builder,
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): Retrofit =
        retrofitBuilder
            .client(
                okHttpBuilder
                    .addInterceptor(authInterceptor)
                    .build()
            )
            .baseUrl(BuildConfig.GITHUB_API_BASE_URL)
            .build()
}
