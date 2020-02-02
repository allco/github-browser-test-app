package se.allco.githubbrowser.app.login.autologin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.ui.delayedSpinner
import se.allco.githubbrowser.common.utils.toLiveData
import javax.inject.Inject

class AutoLoginViewModel @Inject constructor(
    model: AutoLoginModel,
    application: Application
) : AndroidViewModel(application) {

    val showLoading = MutableLiveData(false)

    val result: LiveData<User> =
        model
            .login()
            .delayedSpinner(showLoading)
            .toLiveData()
}
