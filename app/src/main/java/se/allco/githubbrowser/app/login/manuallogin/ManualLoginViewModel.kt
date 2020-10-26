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
import se.allco.githubbrowser.app.login.manuallogin.github_client.GithubWebClient
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.utils.getString
import se.allco.githubbrowser.common.utils.map
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber
import javax.inject.Inject

class ManualLoginViewModel @Inject constructor(
    application: Application,
    val webClient: GithubWebClient,
    private val model: ManualLoginModel,
) : AndroidViewModel(application) {

    private sealed class State {
        object Initializing : State()
        object Loading : State()
        object ShowContent : State()
        data class Error(@StringRes val messageRes: Int) : State()
    }

    companion object {
        private fun GithubWebClient.Event.asViewModelState(): State = when (this) {
            GithubWebClient.Event.PageLoadingStarted -> State.Loading
            GithubWebClient.Event.PageLoadingSuccess -> State.ShowContent
            is GithubWebClient.Event.PageLoadingError -> State.Error(messageRes)
            is GithubWebClient.Event.GithubCodeReceived -> State.Loading
        }
    }

    private val disposables = CompositeDisposable()

    val showLoading = MutableLiveData(false)
    val showContent = MutableLiveData(false)
    val errorMessage = MutableLiveData<String>(null)
    val showError: LiveData<Boolean> = errorMessage.map { it != null }

    private val retrySubject = BehaviorSubject.createDefault(Unit)

    val result: LiveData<User.Valid> =
        retrySubject
            .switchMap {
                waitForGithubCode()
                    .switchMapSingle(::waitForValidUser)
                    .onErrorResumeNext(::createErrorHandler)
            }
            .toLiveData(disposables)

    fun onRetryClicked() = retrySubject.onNext(Unit)

    private fun waitForGithubCode(): Observable<GithubCode> =
        webClient
            .states
            .takeUntil { it is GithubWebClient.Event.GithubCodeReceived }
            .doOnNext { it.asViewModelState().let(::renderState) }
            .ofType(GithubWebClient.Event.GithubCodeReceived::class.java)
            .map { it.code }

    private fun waitForValidUser(githubCode: GithubCode): Single<User.Valid> =
        model.processCode(githubCode)

    private fun createErrorHandler(err: Throwable): Observable<User.Valid> {
        Timber.w(err, "ManualLoginViewModel failed")
        return model
            .createErrorHandler()
            .doOnSubscribe { renderState(State.Error(R.string.login_manual_error_user_data_fetching)) }
            .andThen(Observable.never())
    }

    private fun renderState(state: State) {
        Timber.v("renderState() $state")
        when (state) {
            State.Initializing -> {
                showContent.postValue(false)
                showLoading.postValue(false)
                errorMessage.postValue(null)
            }
            State.Loading -> {
                showContent.postValue(false)
                errorMessage.postValue(null)
                showLoading.postValue(true)
            }
            State.ShowContent -> {
                showLoading.postValue(false)
                errorMessage.postValue(null)
                showContent.postValue(true)
            }
            is State.Error -> {
                showContent.postValue(false)
                showLoading.postValue(false)
                errorMessage.postValue(getString(state.messageRes))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
