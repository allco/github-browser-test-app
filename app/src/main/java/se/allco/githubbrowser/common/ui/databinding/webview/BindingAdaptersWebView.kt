package se.allco.githubbrowser.common.ui.databinding.webview

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.databinding.BindingAdapter

@BindingAdapter("destination")
fun setWebViewDestination(view: WebView, destination: WebViewDestination?) {
    destination?.apply {
        view.loadUrl(destination.url, destination.headers)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@BindingAdapter("settings")
fun setWebViewSettings(view: WebView, settings: WebViewSettings?) {
    if (settings == null) return
    view.webViewClient = settings.webClient
    view.webChromeClient = settings.chromeClient

    when (settings.useCache) {
        true -> view.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        else -> view.settings.cacheMode = WebSettings.LOAD_NO_CACHE
    }

    view.settings.apply {
        setAppCachePath(view.context.cacheDir.toString())
        setAppCacheEnabled(settings.useCache)
        javaScriptEnabled = settings.javaScriptEnabled
        domStorageEnabled = settings.domStorageEnabled
        allowContentAccess = settings.allowContentAccess
        setGeolocationEnabled(settings.geolocationEnabled)
        setSupportZoom(settings.zoomEnabled)
        @Suppress("DEPRECATION")
        pluginState = WebSettings.PluginState.ON
        allowFileAccessFromFileURLs = settings.allowFileAccess
        allowUniversalAccessFromFileURLs = settings.allowFileAccess
    }
}
