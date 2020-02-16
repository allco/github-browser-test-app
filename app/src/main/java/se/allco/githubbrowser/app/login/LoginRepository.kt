package se.allco.githubbrowser.app.login

import se.allco.githubbrowser.app.user.User

interface LoginRepository {
    fun switchLoggedInUser(user: User.Valid)
}
