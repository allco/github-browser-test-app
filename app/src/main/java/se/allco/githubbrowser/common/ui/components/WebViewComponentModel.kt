package se.allco.githubbrowser.common.ui.components

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import se.allco.githubbrowser.common.NetworkReporter
import se.allco.githubbrowser.common.ui.databinding.webview.FileChooserRequest
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewDestination
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewSettings
import javax.inject.Inject
import javax.inject.Provider

interface WebViewComponentModel {
    val settings: WebViewSettings
    val destination: LiveData<WebViewDestination>
    val showContent: LiveData<Boolean>
    val showLoading: LiveData<Boolean>
    val errorMessage: LiveData<String>

    enum class Result {
        CONSUMED_FINISHING,
        CONSUMED,
        IGNORED
    }

    @Suppress("MemberVisibilityCanBePrivate")
    class Builder @Inject constructor(
        private val context: Context,
        private val networkReporterProvider: Provider<NetworkReporter>
    ) {
        var networkState: Observable<Boolean>? = null
        var onChooseFile: ((FileChooserRequest) -> Maybe<Array<Uri>>)? = null
        var overrideLoading: ((uri: Uri) -> Result)? = null
        var javaScriptEnabled = false
        var useCache = false

        fun build(
            url: String,
            headers: Map<String, String>,
            scopedDisposable: CompositeDisposable
        ): WebViewComponentModel =
            WebViewComponentModelImpl(
                url = url,
                headers = headers,
                context = context,
                useCache = useCache,
                javaScriptEnabled = javaScriptEnabled,
                networkState = networkState ?: networkReporterProvider.get().states(),
                overrideLoading = overrideLoading,
                onChooseFile = onChooseFile,
                disposables = scopedDisposable
            )
    }
}
