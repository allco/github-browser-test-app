package se.allco.githubbrowser.app.main.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import se.allco.githubbrowser.R
import se.allco.githubbrowser.common.ui.delayedSpinner
import se.allco.githubbrowser.common.utils.combine
import se.allco.githubbrowser.common.utils.getString
import se.allco.githubbrowser.common.utils.map
import se.allco.githubbrowser.common.utils.plusAssign
import se.allco.githubbrowser.common.utils.subscribeSafely
import se.allco.githubbrowser.common.utils.toLiveData
import javax.inject.Inject

class AccountViewModel @Inject constructor(
    application: Application,
    private val repository: AccountRepository
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()
    private val _showLoading = MutableLiveData(false)

    val userName: LiveData<String> =
        repository
            .getUserName()
            .delayedSpinner(_showLoading)
            .doOnError { errorMessage.postValue(getString(R.string.error_generic)) }
            .toLiveData()

    val errorMessage = MutableLiveData<String>(null)
    val showError: LiveData<Boolean> = errorMessage.map { !it.isNullOrBlank() }

    val showLoading: LiveData<Boolean> =
        _showLoading
            .combine(showError, false) { loading, error -> loading == true && error != true }

    val showContent: LiveData<Boolean> =
        userName
            .combine(showLoading, false) { content, loading -> content != null && loading != true }
            .combine(showError, false) { content, error -> content == true && error != true }

    fun onLogout() {
        disposables +=
            repository
                .logoutUser()
                .delayedSpinner(_showLoading)
                .doOnError { errorMessage.postValue(getString(R.string.error_generic)) }
                .subscribeSafely()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
