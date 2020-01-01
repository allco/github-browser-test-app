package se.allco.githubbrowser.app.login

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserRepository
import javax.inject.Inject

@JvmSuppressWildcards
class LoginRepository @Inject constructor(
    private val retrofitFactory: (baseUrl: String) -> Retrofit,
    private val userRepository: UserRepository,
    private val tokenCache: TokenCache
) {

    interface GetCurrentUserInfo {
        data class Response(val id: String)

        @GET("/user")
        fun call(@Header("Authorization") authHeader: String): Single<Response>
    }

    fun fetchGithubUser(token: GithubToken): Single<User.Valid> =
        retrofitFactory(BuildConfig.GITHUB_API_BASE_URL)
            .create(GetCurrentUserInfo::class.java)
            .call(token.asAuthHeader())
            .subscribeOn(Schedulers.io())
            .map { User.Valid(token = token, userId = it.id) }

    fun readCachedToken(): Maybe<GithubToken> =
        tokenCache.read()

    fun writeCachedToken(user: User.Valid): Completable =
        tokenCache.write(user)

    fun switchLoggedInUser(user: User.Valid) {
        userRepository.switchUser(user)
    }
}

fun GithubToken.asAuthHeader(): String = "token $this"
