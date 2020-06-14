package se.allco.githubbrowser.app.user

typealias GithubToken = String

sealed class User {
    data class Valid(
        val userId: String,
        val userName: String,
        val token: GithubToken,
        val imageUrl: String?
    ) : User()

    object Invalid : User()
}
