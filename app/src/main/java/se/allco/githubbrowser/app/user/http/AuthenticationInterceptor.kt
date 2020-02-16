package se.allco.githubbrowser.app.user.http

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user._di.UserScope
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject

@UserScope
class AuthenticationInterceptor @Inject constructor(private val user: User) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response =
        when (user) {
            is User.Valid ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .header("Authorization", "token ${user.token}")
                        .build()
                )
            User.Invalid ->
                Response
                    .Builder()
                    .header("X-reason", "AuthInterceptor. The user is logged out.")
                    .code(HTTP_UNAUTHORIZED)
                    .request(chain.request())
                    .protocol(Protocol.HTTP_2)
                    .message("<Request failed. User is unauthenticated.>")
                    .body("<Request failed. User is unauthenticated.>".toResponseBody("*/*".toMediaType()))
                    .build()
        }
}

