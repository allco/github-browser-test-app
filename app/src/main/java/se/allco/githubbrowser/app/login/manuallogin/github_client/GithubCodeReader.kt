package se.allco.githubbrowser.app.login.manuallogin.github_client

import android.net.Uri
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.app.login.manuallogin.GithubCode
import javax.inject.Inject

class GithubCodeReader @Inject constructor() {

    fun readCode(uri: Uri, requestId: String): GithubCode? {
        if (uri.scheme != BuildConfig.APP_SCHEMA) return null
        val state = uri.getQueryParameter("state")
        val code = uri.getQueryParameter("code")
        return when {
            state == requestId && code != null -> return code
            else -> code
        }
    }
}