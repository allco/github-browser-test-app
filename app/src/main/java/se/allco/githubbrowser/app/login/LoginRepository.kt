package se.allco.githubbrowser.app.login

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import se.allco.githubbrowser.BuildConfig
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
) {

    interface GetCurrentUserInfo {
        data class Response(val id: String, val name: String?, val login: String)

        @GET("/user")
        fun call(@Header("Authorization") authHeader: String): Single<Response>
    }

    fun fetchGithubUser(token: GithubToken): Single<User.Valid> =
        retrofitBuilder
            .client(okHttpBuilder.build())
            .baseUrl(BuildConfig.GITHUB_API_BASE_URL)
            .build()
            .create(GetCurrentUserInfo::class.java)
            .call(token.asAuthHeader())
            .subscribeOn(Schedulers.io())
            .map { User.Valid(token = token, userId = it.id, userName = it.name ?: it.login) }

    fun readCachedToken(): Maybe<GithubToken> =
        tokenCache.read()

    fun writeCachedToken(user: User.Valid): Completable =
        tokenCache.write(user)

    fun switchLoggedInUser(user: User.Valid) {
        userModel.switchUser(user)
    }
}

fun GithubToken.asAuthHeader(): String = "token $this"
