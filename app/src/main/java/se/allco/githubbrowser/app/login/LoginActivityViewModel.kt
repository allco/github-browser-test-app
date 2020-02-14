package se.allco.githubbrowser.app.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import se.allco.githubbrowser.app.login.autologin.AutoLoginFragment
import se.allco.githubbrowser.app.login.di.LoginScope
import se.allco.githubbrowser.app.login.manuallogin.ManualLoginFragment
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.ui.toSingleLiveEvent
import javax.inject.Inject

@LoginScope
class LoginActivityViewModel @Inject constructor(
    application: Application,
    private val repository: LoginRepository
) : AndroidViewModel(application), AutoLoginFragment.Listener, ManualLoginFragment.Listener {

    private val _launchManualLogin = MutableLiveData<Unit>()
    val launchManualLogin: LiveData<Unit> = _launchManualLogin.toSingleLiveEvent()
    val loggedInUser = MutableLiveData<User.Valid>()

    init {
        loggedInUser.observeForever { repository.switchLoggedInUser(it) }
    }

    override fun onAutoLoginResult(user: User) {
        if (user is User.Valid) {
            loggedInUser.postValue(user)
        } else {
            _launchManualLogin.postValue(Unit)
        }
    }

    override fun onManualLoginResult(user: User.Valid) {
        loggedInUser.postValue(user)
    }
}
