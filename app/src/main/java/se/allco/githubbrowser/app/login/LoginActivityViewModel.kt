package se.allco.githubbrowser.app.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.common.ui.components.WebViewComponentModel
import se.allco.githubbrowser.common.ui.delayedSpinner
import se.allco.githubbrowser.common.utils.combineLiveData
import se.allco.githubbrowser.common.utils.getString
import se.allco.githubbrowser.common.utils.map
import se.allco.githubbrowser.common.utils.plusAssign
import se.allco.githubbrowser.common.utils.subscribeSafely
import timber.log.Timber
import javax.inject.Inject

class LoginActivityViewModel @Inject constructor(
    model: LoginModel,
    application: Application,
    private val githubLoginWebView: GithubLoginWebViewModel
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    // Indicates the logged in user. View has to finish it is not null.
    val finished = MutableLiveData<Unit>()

    // Error message parameters
    val errorMessage = MutableLiveData<String>(null)
    val showError: LiveData<Boolean> = errorMessage.map { !it.isNullOrBlank() }

    // Loading spinner parameters
    private val showLoadingInternal = MutableLiveData(false)
    val showLoading =
        combineLiveData(showError, showLoadingInternal, false)
        { error, loading -> error != true && loading == true }

    // GitHub login web page parameters
    val webComponentViewModel = MutableLiveData<WebViewComponentModel>(null)
    val showGithubLogin: LiveData<Boolean> =
        combineLiveData(showError, webComponentViewModel, false)
        { error, model -> error != true && model != null }

    fun onRetryClicked() {
        errorMessage.postValue(null)
        retrySubject.onNext(Unit)
    }

    private val retrySubject = PublishSubject.create<Unit>()

    init {
        disposables +=
            model
                .loginUser()
                .delayedSpinner(showLoadingInternal)
                .doOnError(::onModelError)
                .doOnNext(::onModelSignal)
                .retryWhen { retrySubject }
                .subscribeSafely()
    }

    private fun onModelError(err: Throwable) {
        Timber.w(err, "LoginActivityViewModel.login failed")
        errorMessage.postValue(getString(R.string.error_generic))
    }

    private fun onModelSignal(signal: LoginModel.Signal) {
        when (signal) {
            is LoginModel.Signal.RunManualLogin -> onManualLogin(signal.callback)
            LoginModel.Signal.Success -> finished.postValue(Unit)
        }
    }

    private fun onManualLogin(callback: SingleSubject<GithubToken>) {
        webComponentViewModel.postValue(
            githubLoginWebView.createWebViewModel(disposables, callback)
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
