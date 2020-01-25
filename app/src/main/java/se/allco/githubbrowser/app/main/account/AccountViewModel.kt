package se.allco.githubbrowser.app.main.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.app.user.UserRepository
import se.allco.githubbrowser.common.utils.toLiveData
import javax.inject.Inject

class AccountViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    val userName: LiveData<String> =
        userRepository
            .userFeed
            .ofType(User.Valid::class.java)
            .map { it.userName }
            .toLiveData()

    fun onLogout() {
        userRepository.logoutUser()
    }
}
