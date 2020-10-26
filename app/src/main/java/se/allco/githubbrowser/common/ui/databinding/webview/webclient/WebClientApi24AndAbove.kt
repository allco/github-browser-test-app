package se.allco.githubbrowser.common.ui.databinding.webview.webclient

import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView

class WebClientApi24AndAbove(
    private val loadingInterceptor: (Uri) -> LoadingCommand,
) : WebClient() {

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        err: WebResourceError?,
    ) {
        postState(State.ERROR)
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean {
        val uri = request?.url ?: return false
        return loadingInterceptor(uri) == LoadingCommand.BREAK_LOADING
    }
}