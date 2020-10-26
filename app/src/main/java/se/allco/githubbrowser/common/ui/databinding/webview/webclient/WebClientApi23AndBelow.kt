package se.allco.githubbrowser.common.ui.databinding.webview.webclient

import android.net.Uri
import android.webkit.WebView

class WebClientApi23AndBelow(
    private val loadingInterceptor: (Uri) -> LoadingCommand,
) : WebClient() {

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?,
    ) {
        postState(State.ERROR)
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val uri = url?.let(Uri::parse) ?: return false
        return loadingInterceptor(uri) == LoadingCommand.BREAK_LOADING
    }
}