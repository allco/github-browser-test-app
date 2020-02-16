package se.allco.githubbrowser.app.main.repos

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.common.ui.delayedSpinner
import se.allco.githubbrowser.common.utils.combine
import se.allco.githubbrowser.common.utils.getString
import se.allco.githubbrowser.common.utils.map
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber
import javax.inject.Inject

class ReposViewModel @Inject constructor(
    application: Application,
    reposRepository: ReposRepository,
    reposItemViewModelFactory: ReposItemViewModel.Factory
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    private sealed class State {
        object Initializing : State()
        object Loading : State()
        object ShowContent : State()
        class Error(@StringRes val messageRes: Int, val allowRetry: Boolean = true) : State()
    }

    // Error message parameters
    val errorMessage = MutableLiveData<String>(null)
    val showError: LiveData<Boolean> =
        errorMessage.map { it != null }
    private val _showRetryButton = MutableLiveData(false)
    val showRetryButton =
        _showRetryButton.combine(showError, false) { retry, error -> retry == true && error == true }

    // Loading spinner parameters
    private val _showLoading = MutableLiveData(false)
    val showLoading: LiveData<Boolean> =
        _showLoading.combine(showError, false) { loading, error -> error != true && loading == true }

    // WebView readiness parameters
    private val _showContent = MutableLiveData(false)
    val showContent: LiveData<Boolean> =
        _showContent
            .combine(showLoading, false) { content, loading -> content == true && loading != true }
            .combine(showError, false) { content, error -> content == true && error != true }

    private val retrySubject = BehaviorSubject.createDefault(Unit)

    val listItems: LiveData<List<ReposItemViewModel>> =
        retrySubject
            .switchMapSingle {
                reposRepository
                    .getRepos()
                    .map { listRepos ->
                        listRepos.map { reposItemViewModelFactory.create(it) }
                    }
                    .doOnSubscribe { renderState(State.Initializing) }
                    .delayedSpinner { loading -> if (loading) renderState(State.Loading) }
                    .doOnSuccess { list ->
                        if (list.isEmpty()) renderState(
                            State.Error(R.string.main_repos_no_repos_found, allowRetry = false)
                        )
                        else renderState(State.ShowContent)
                    }
                    .onErrorResumeNext(::createErrorHandler)
            }
            .toLiveData()

    private fun createErrorHandler(err: Throwable): Single<List<ReposItemViewModel>> {
        Timber.w(err, "ReposViewModel failed")
        return Single
            .never<List<ReposItemViewModel>>()
            .doOnSubscribe { renderState(State.Error(R.string.main_repos_error_fetching_data)) }
    }

    private fun renderState(state: State) {
        when (state) {
            State.Initializing -> {
                _showRetryButton.postValue(false)
                _showContent.postValue(false)
                _showLoading.postValue(false)
                errorMessage.postValue(null)
            }
            State.Loading -> {
                _showRetryButton.postValue(false)
                _showContent.postValue(false)
                errorMessage.postValue(null)
                _showLoading.postValue(true)
            }
            State.ShowContent -> {
                _showRetryButton.postValue(false)
                _showLoading.postValue(false)
                errorMessage.postValue(null)
                _showContent.postValue(true)
            }
            is State.Error -> {
                _showContent.postValue(false)
                _showLoading.postValue(false)
                _showRetryButton.postValue(state.allowRetry)
                errorMessage.postValue(getString(state.messageRes))
            }
        }
    }

    fun onRetryClicked() {
        retrySubject.onNext(Unit)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
