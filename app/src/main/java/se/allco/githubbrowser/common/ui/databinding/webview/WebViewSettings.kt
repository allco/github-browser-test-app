package se.allco.githubbrowser.common.ui.databinding.webview

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import se.allco.githubbrowser.common.utils.subscribeSafely

data class FileChooserRequest(
    val type: String?,
    val title: String?,
    val allowMultiple: Boolean
)

class WebViewSettings(
    val overrideLoading: (String?) -> Boolean,
    val useCache: Boolean,
    val javaScriptEnabled: Boolean = false,
    val domStorageEnabled: Boolean = false,
    val allowContentAccess: Boolean = false,
    val geolocationEnabled: Boolean = false,
    val allowFileAccess: Boolean = false,
    val zoomEnabled: Boolean = false,
    val onChooseFile: ((FileChooserRequest) -> Maybe<Array<Uri>>)? = null
) {
    enum class State { STARTED, FINISHED, ERROR }

    val webClient = WebClient()
    val chromeClient = ChromeClient()
    private val statesInternal: PublishSubject<State> = PublishSubject.create()
    val states: Observable<State> = statesInternal

    inner class WebClient : WebViewClient() {

        private val isAPI24AndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            if (!isAPI24AndAbove) statesInternal.onNext(State.ERROR)
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, err: WebResourceError?) {
            if (isAPI24AndAbove) statesInternal.onNext(State.ERROR)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            statesInternal.onNext(State.STARTED)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            statesInternal.onNext(State.FINISHED)
        }

        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
            if (!isAPI24AndAbove) overrideLoading.invoke(url) else false

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
            if (isAPI24AndAbove) overrideLoading.invoke(request?.url?.toString()) else false
    }

    inner class ChromeClient : WebChromeClient() {

        override fun onPermissionRequest(request: PermissionRequest) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                request.grant(request.resources) // We should have permission already
            }
        }

        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (filePathCallback == null) return false
            val request = FileChooserRequest(
                type = fileChooserParams?.acceptTypes?.joinToString(","),
                title = fileChooserParams?.title?.toString(),
                allowMultiple = fileChooserParams?.mode == FileChooserParams.MODE_OPEN_MULTIPLE
            )

            val disposable = when {
                onChooseFile != null -> onChooseFile.invoke(request).subscribeSafely {
                    onSuccess = { filePathCallback.onReceiveValue(it) }
                    onComplete = { filePathCallback.onReceiveValue(null) }
                }
                else -> null.also {
                    filePathCallback.onReceiveValue(null)
                }
            }

            webView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View?) {
                    disposable?.dispose()
                }

                override fun onViewAttachedToWindow(v: View?) {}
            })

            return true
        }
    }
}
