package se.allco.githubbrowser.app.login.autologin

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.app.user.User

interface AutoLoginRepository {
    fun readCachedToken(): Maybe<GithubToken>
    fun fetchUserData(token: GithubToken): Single<User.Valid>
    fun clearUserData(): Completable
}
