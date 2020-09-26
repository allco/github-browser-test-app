package se.allco.githubbrowser.app.login.autologin

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.app.user.User

interface AutoLoginRepository {
    fun readCachedToken(): Maybe<GithubToken>
    fun fetchUserData(token: GithubToken): Single<User.Valid>
    fun clearUserData(): Completable
}
