package se.allco.githubbrowser.app.login

import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.app.login.autologin.AutoLoginRepository
import se.allco.githubbrowser.app.login.manuallogin.ManualLoginRepository
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.app.user.TokenCache
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserModel
import javax.inject.Inject

@JvmSuppressWildcards
class LoginRepository @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder,
    private val okHttpBuilder: OkHttpClient.Builder,
    private val userModel: UserModel,
    private val tokenCache: TokenCache
) : AutoLoginRepository, ManualLoginRepository {

    interface GetCurrentUserInfo {
        data class Response(val id: String, val name: String?, val login: String)

        @GET("/user")
        fun call(@Header("Authorization") authHeader: String): Single<Response>
    }

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

    override fun fetchAccessToken(code: String): Single<GithubToken> =
        retrofitBuilder
            .client(okHttpBuilder.build())
            .baseUrl(BuildConfig.GITHUB_BASE_URL)
            .build()
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

    override fun fetchUserData(token: GithubToken): Single<User.Valid> =
        retrofitBuilder
            .client(okHttpBuilder.build())
            .baseUrl(BuildConfig.GITHUB_API_BASE_URL)
            .build()
            .create(GetCurrentUserInfo::class.java)
            .call(token.asAuthHeader())
            .subscribeOn(Schedulers.io())
            .map { User.Valid(token = token, userId = it.id, userName = it.name ?: it.login) }

    override fun readCachedToken(): Maybe<GithubToken> =
        tokenCache.read()

    override fun clearUserData(): Completable =
        userModel.logoutUser()

    override fun writeCachedToken(token: GithubToken): Completable =
        tokenCache.write(token)

    fun switchLoggedInUser(user: User.Valid) {
        userModel.switchUser(user)
    }
}

fun GithubToken.asAuthHeader(): String = "token $this"
