package se.allco.githubbrowser.common.ui.databinding.webview

import android.net.Uri
import android.os.Build
import io.reactivex.rxjava3.core.Observable
import se.allco.githubbrowser.common.ui.databinding.webview.webclient.WebClient
import se.allco.githubbrowser.common.ui.databinding.webview.webclient.WebClientApi23AndBelow
import se.allco.githubbrowser.common.ui.databinding.webview.webclient.WebClientApi24AndAbove

class WebViewSettings(
    val useCache: Boolean = false,
    val javaScriptEnabled: Boolean = false,
    val domStorageEnabled: Boolean = false,
    val allowContentAccess: Boolean = false,
    val geolocationEnabled: Boolean = false,
    val allowFileAccess: Boolean = false,
    val zoomEnabled: Boolean = false,
) {
    companion object {
        private val isAPI24AndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        val defaultInterceptor: (Uri) -> WebClient.LoadingCommand =
            { WebClient.LoadingCommand.CONTINUE_LOADING }
    }

    val webClient: WebClient = when {
        isAPI24AndAbove -> WebClientApi24AndAbove(::onLoadPage)
        else -> WebClientApi23AndBelow(::onLoadPage)
    }

    private var loadingInterceptor: (Uri) -> WebClient.LoadingCommand = defaultInterceptor

    val states: Observable<WebClient.State>
        get() = webClient.states.removeStateFinishedAfterStateError()

    fun resetLoadingInterceptor(interceptor: (Uri) -> WebClient.LoadingCommand) {
        loadingInterceptor = interceptor
    }

    fun resetLoadingInterceptor() {
        loadingInterceptor = defaultInterceptor
    }

    private fun onLoadPage(uri: Uri): WebClient.LoadingCommand {
        return loadingInterceptor(uri)
    }

    private fun Observable<WebClient.State>.removeStateFinishedAfterStateError(): Observable<WebClient.State> {
        return scan { prev, next ->
            when {
                prev == WebClient.State.ERROR && next == WebClient.State.FINISHED -> WebClient.State.ERROR
                else -> next
            }
        }.distinctUntilChanged()
    }
}






