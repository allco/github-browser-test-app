package se.allco.githubbrowser.app.uicomponents.webview

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import se.allco.githubbrowser.common.NetworkReporter
import se.allco.githubbrowser.common.ui.databinding.webview.FileChooserRequest
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewDestination
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewSettings
import javax.inject.Inject
import javax.inject.Provider

interface WebViewModel {
    val settings: WebViewSettings
    val destination: LiveData<WebViewDestination>
    val showContent: LiveData<Boolean>
    val showLoading: LiveData<Boolean>
    val errorMessage: LiveData<String>

    enum class Result {
        CONSUME_AND_FINISH,
        CONSUME,
        IGNORE
    }

    class Builder @Inject constructor(
        private val context: Context,
        private val networkReporterProvider: Provider<NetworkReporter>
    ) {
        var authToken: Single<String>? = null
        var networkState: Observable<Boolean>? = null
        var onChooseFile: ((FileChooserRequest) -> Maybe<Array<Uri>>)? = null
        var overrideLoading: ((uri: Uri) -> Result)? = null

        fun build(urlFeed: Observable<String>, compositeDisposable: CompositeDisposable): WebViewModel =
            WebViewModelImpl(
                urlFeed = urlFeed,
                context = context,
                networkState = networkState ?: networkReporterProvider.get().states(),
                overrideLoading = overrideLoading,
                onChooseFile = onChooseFile,
                disposables = compositeDisposable
            )
    }
}
