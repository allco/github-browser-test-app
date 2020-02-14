package se.allco.githubbrowser.app.login.manuallogin

import io.reactivex.Completable
import io.reactivex.Single
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.app.user.User

interface ManualLoginRepository {
    fun fetchAccessToken(code: String): Single<GithubToken>
    fun fetchUserData(token: GithubToken): Single<User.Valid>
    fun writeCachedToken(token: GithubToken): Completable
    fun clearUserData(): Completable
}
