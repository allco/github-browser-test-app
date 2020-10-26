package se.allco.githubbrowser.app.login.manuallogin.github_client

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import githubbrowser.common.utils.dump
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.login.manuallogin.GithubCode
import se.allco.githubbrowser.common.network_reporter.NetworkReporter
import se.allco.githubbrowser.common.ui.addSmartLoadingState
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewDestination
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewSettings
import se.allco.githubbrowser.common.ui.databinding.webview.webclient.WebClient
import se.allco.githubbrowser.common.utils.timeoutFirst
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class GithubWebClient @Inject constructor(
    networkReporter: NetworkReporter,
    private val reader: GithubCodeReader,
) {
    sealed class Event {
        object PageLoadingStarted : Event()
        object PageLoadingSuccess : Event()
        data class PageLoadingError(@StringRes val messageRes: Int) : Event()
        data class GithubCodeReceived(val code: GithubCode) : Event()
    }

    // WebViewInput
    val settings = WebViewSettings(javaScriptEnabled = true)
    val destination = MutableLiveData<WebViewDestination>()
    // GithubOutput
    val states: Observable<Event> =
        networkReporter
            .states()
            .switchMap(::onNetworkStateChanged)

    private val accessCodeSubject = PublishSubject.create<Event.GithubCodeReceived>()

    private fun onNetworkStateChanged(online: Boolean): Observable<Event> =
        if (online) onInternetAvailable() else onInternetUnavailable()

    private fun onInternetAvailable(): Observable<Event> =
        settings
            .webClient
            .states
            .dump("GithubWebClient, tag_web_client_states")
            .timeoutFirst(timeMs = 2000)
            .asViewModelStates()
            .addSmartLoadingState { Event.PageLoadingStarted }
            .mergeWith(accessCodeSubject)
            .doOnSubscribe { loadAuthenticationPage() }
            .dump("GithubWebClient, tag_result")

    private fun loadAuthenticationPage() {
        val requestId = UUID.randomUUID().toString()
        val destination = createDestination(requestId)
        this.destination.postValue(destination)
        settings.resetLoadingInterceptor { uri -> onNavigateToUrl(uri, requestId) }
    }

    private fun onNavigateToUrl(uri: Uri, requestId: String): WebClient.LoadingCommand {
        Timber.v("onNavigateToUrl() called with: uri = $uri, requestId = $requestId")
        return reader.readCode(uri, requestId)?.let { code ->
            onCodeReceived(code)
            WebClient.LoadingCommand.BREAK_LOADING
        } ?: WebClient.LoadingCommand.CONTINUE_LOADING
    }

    private fun onCodeReceived(code: GithubCode) {
        accessCodeSubject.onNext(Event.GithubCodeReceived(code))
    }

    private fun onInternetUnavailable(): Observable<Event> =
        Observable.just(Event.PageLoadingError(R.string.login_manual_error_no_network_connection))

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

    companion object {
        private fun Observable<WebClient.State>.asViewModelStates(): Observable<Event> =
            switchMap { webPageState ->
                when (webPageState) {
                    WebClient.State.FINISHED -> Observable.just(Event.PageLoadingSuccess)
                    WebClient.State.ERROR -> Observable.just(Event.PageLoadingError(R.string.login_manual_error_web_loading))
                    else -> Observable.never()
                }
            }
    }
}
