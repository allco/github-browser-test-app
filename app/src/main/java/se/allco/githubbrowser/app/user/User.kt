package se.allco.githubbrowser.app.user

typealias GithubToken = String

sealed class User {
    data class Valid(
        val userId: String,
        val token: GithubToken
    ) : User()

    object Invalid : User()
}
