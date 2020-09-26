package se.allco.githubbrowser.app.login.manuallogin

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.ui.delayedSpinner
import se.allco.githubbrowser.common.utils.combine
import se.allco.githubbrowser.common.utils.getString
import se.allco.githubbrowser.common.utils.map
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber
import javax.inject.Inject

class ManualLoginViewModel @Inject constructor(
    application: Application,
    val webViewModel: GithubLoginWebViewModel,
    private val model: ManualLoginModel
) : AndroidViewModel(application) {

    private sealed class State {
        object Initializing : State()
        object Loading : State()
        object ShowContent : State()
        class Error(@StringRes val messageRes: Int) : State()
    }

    private val disposables = CompositeDisposable()

    // Error message parameters
    val errorMessage = MutableLiveData<String>(null)
    val showError: LiveData<Boolean> =
        errorMessage.map { it != null }

    // Loading spinner parameters
    private val _showLoading = MutableLiveData(false)
    val showLoading: LiveData<Boolean> =
        _showLoading.combine(
            showError,
            false
        ) { loading, error -> error != true && loading == true }

    // WebView readiness parameters
    private val _showContent = MutableLiveData(false)
    val showContent: LiveData<Boolean> =
        _showContent
            .combine(showLoading, false) { content, loading -> content == true && loading != true }
            .combine(showError, false) { content, error -> content == true && error != true }

    private val retrySubject = BehaviorSubject.createDefault(Unit)

    fun onRetryClicked() {
        retrySubject.onNext(Unit)
    }

    val result: LiveData<User.Valid> =
        retrySubject
            .switchMap {
                waitForGithubCode()
                    .switchMapSingle(::waitForValidUser)
                    .onErrorResumeNext(::createErrorHandler)
            }
            .toLiveData(disposables)

    private fun waitForValidUser(githubCode: GithubCode): Single<User.Valid> =
        model
            .processCode(githubCode)
            .delayedSpinner(_showLoading)
            .doOnSubscribe { renderState(State.Initializing) }

    private fun waitForGithubCode(): Observable<GithubCode> {
        return webViewModel
            .states
            .takeUntil { it is GithubLoginWebViewModel.State.ResultCode }
            .doOnNext { it.asViewModelState()?.let(::renderState) }
            .ofType(GithubLoginWebViewModel.State.ResultCode::class.java)
            .map { it.code }
    }

    private fun createErrorHandler(err: Throwable): Observable<User.Valid> {
        Timber.w(err, "ManualLoginViewModel failed")
        return model
            .createErrorHandler()
            .doOnSubscribe { renderState(State.Error(R.string.login_manual_error_user_data_fetching)) }
            .andThen(Observable.never())
    }

    private fun GithubLoginWebViewModel.State.asViewModelState(): State? =
        when (this) {
            GithubLoginWebViewModel.State.Initializing -> State.Initializing
            GithubLoginWebViewModel.State.Loading -> State.Loading
            GithubLoginWebViewModel.State.LoadingFinished -> State.ShowContent
            is GithubLoginWebViewModel.State.Error -> State.Error(messageRes)
            else -> null
        }

    private fun renderState(state: State) {
        when (state) {
            State.Initializing -> {
                _showContent.postValue(false)
                _showLoading.postValue(false)
                errorMessage.postValue(null)
            }
            State.Loading -> {
                _showContent.postValue(false)
                errorMessage.postValue(null)
                _showLoading.postValue(true)
            }
            State.ShowContent -> {
                _showLoading.postValue(false)
                errorMessage.postValue(null)
                _showContent.postValue(true)
            }
            is State.Error -> {
                _showContent.postValue(false)
                _showLoading.postValue(false)
                errorMessage.postValue(getString(state.messageRes))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
