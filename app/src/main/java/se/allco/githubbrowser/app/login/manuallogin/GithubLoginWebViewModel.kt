package se.allco.githubbrowser.app.login.manuallogin

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.login.manuallogin.GithubLoginWebViewModel.State
import se.allco.githubbrowser.common.NetworkReporter
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewDestination
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewSettings
import se.allco.githubbrowser.common.ui.delayedSpinner
import java.util.*
import javax.inject.Inject

interface GithubLoginWebViewModel {
    val destination: MutableLiveData<WebViewDestination>
    val settings: WebViewSettings
    val states: Observable<State>

    sealed class State {
        object Initializing : State()
        object Loading : State()
        object LoadingFinished : State()
        data class ResultCode(val code: String) : State()
        data class Error(@StringRes val messageRes: Int) : State()
    }
}

class GithubLoginWebViewModelImpl @Inject constructor(
    networkReporter: NetworkReporter
) : GithubLoginWebViewModel {

    private val stateInjector = PublishSubject.create<State>()

    override val settings = WebViewSettings(javaScriptEnabled = true)
    override val destination = MutableLiveData<WebViewDestination>()
    override val states: Observable<State> =
        networkReporter
            .states()
            .switchMap(::onNetworkStateChanged)
            .mergeWith(stateInjector.observeOn(AndroidSchedulers.mainThread()))
            .startWith(State.Initializing)

    private fun onNetworkStateChanged(online: Boolean): Observable<State> =
        if (online) {
            val requestId = UUID.randomUUID().toString()
            settings.overrideLoading = { uri -> onNavigateToUrl(uri, requestId) }
            destination.postValue(createDestination(requestId))
            settings
                .webPageStates
                .asViewModelStates()
                .delayedSpinner { showLoading -> if (showLoading) stateInjector.onNext(State.Loading) }
        } else {
            Observable.just(State.Error(R.string.login_manual_error_no_network_connection))
        }

    private fun onNavigateToUrl(uri: Uri?, requestId: String): Boolean {
        if (uri?.scheme != BuildConfig.APP_SCHEMA) return false
        val state = uri.getQueryParameter("state")
        val code = uri.getQueryParameter("code")
        return when {
            state == requestId && code != null -> true.also { stateInjector.onNext(State.ResultCode(code)) }
            else -> false
        }
    }

    private fun createDestination(requestId: String): WebViewDestination =
        WebViewDestination(
            url = Uri.Builder().apply {
                scheme("https")
                    .authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("state", requestId)
                    .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
            }.build().toString(),
            headers = mapOf("Accept" to "application/vnd.github.machine-man-preview+json")
        )

    private fun Observable<WebViewSettings.State>.asViewModelStates(): Observable<State> =
        switchMap { webPageState ->
            when (webPageState) {
                WebViewSettings.State.FINISHED -> Observable.just(State.LoadingFinished)
                WebViewSettings.State.ERROR -> Observable.just(State.Error(R.string.login_manual_error_web_loading))
                else -> Observable.never()
            }
        }
}
