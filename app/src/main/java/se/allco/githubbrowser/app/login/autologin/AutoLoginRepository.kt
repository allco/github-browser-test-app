package se.allco.githubbrowser.app.login.autologin

import io.reactivex.Maybe
import io.reactivex.Single
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.app.user.User
import javax.inject.Inject

class AutoLoginRepository @Inject constructor() {

    fun readCachedToken(): Maybe<GithubToken> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun fetchGithubUser(token: GithubToken): Single<User.Valid> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
