package se.allco.githubbrowser.app.login

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.app.user.GithubToken
import javax.inject.Inject

@JvmSuppressWildcards
class GithubLoginWebRepository @Inject constructor(
    private val retrofitFactory: (baseUrl: String) -> Retrofit
) {

    interface GetAccessToken {

        data class Response(
            @SerializedName("access_token")
            val token: String
        )

        data class Request(
            @SerializedName("client_id")
            val clientId: String,
            @SerializedName("client_secret")
            val clientSecret: String,
            @SerializedName("code")
            val code: String
        )

        @Headers("Accept: application/json")
        @POST("login/oauth/access_token")
        fun call(@Body body: Request): Single<Response>
    }

    fun fetchAccessToken(code: String): Single<GithubToken> =
        retrofitFactory.invoke(BuildConfig.GITHUB_BASE_URL)
            .create(GetAccessToken::class.java)
            .call(
                GetAccessToken.Request(
                    clientSecret = BuildConfig.GITHUB_CLIENT_SECRET,
                    clientId = BuildConfig.GITHUB_CLIENT_ID,
                    code = code
                )
            )
            .subscribeOn(Schedulers.io())
            .map { it.token }
}
