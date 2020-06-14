package se.allco.githubbrowser.common.ui.databinding.webview

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.*
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
    val useCache: Boolean = false,
    val javaScriptEnabled: Boolean = false,
    val domStorageEnabled: Boolean = false,
    val allowContentAccess: Boolean = false,
    val geolocationEnabled: Boolean = false,
    val allowFileAccess: Boolean = false,
    val zoomEnabled: Boolean = false,
    var overrideLoading: ((Uri?) -> Boolean)? = null,
    var onChooseFile: ((FileChooserRequest) -> Maybe<Array<Uri>>)? = null
) {
    enum class State { STARTED, FINISHED, ERROR }

    val webClient = WebClient()
    val chromeClient = ChromeClient()
    private val _states: PublishSubject<State> = PublishSubject.create()
    val webPageStates: Observable<State>
        get() = _states

    inner class WebClient : WebViewClient() {

        private val isAPI24AndAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            if (!isAPI24AndAbove) _states.onNext(State.ERROR)
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, err: WebResourceError?) {
            if (isAPI24AndAbove) _states.onNext(State.ERROR)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            _states.onNext(State.STARTED)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            _states.onNext(State.FINISHED)
        }

        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
            overrideLoading?.takeIf { !isAPI24AndAbove }?.invoke(url?.let { Uri.parse(it) }) ?: false

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
            overrideLoading?.takeIf { isAPI24AndAbove }?.invoke(request?.url) ?: false
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

            val chooseFile = onChooseFile
            val disposable = when {
                chooseFile != null -> chooseFile.invoke(request).subscribeSafely {
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
