package se.allco.githubbrowser.common.ui.components

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import se.allco.githubbrowser.R
import se.allco.githubbrowser.common.ui.databinding.webview.FileChooserRequest
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewDestination
import se.allco.githubbrowser.common.ui.databinding.webview.WebViewSettings
import se.allco.githubbrowser.common.ui.delayedSpinner
import se.allco.githubbrowser.common.ui.toSingleLiveEvent
import se.allco.githubbrowser.common.utils.combineLiveData
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber

class WebViewComponentModelImpl(
    url: String,
    useCache: Boolean,
    javaScriptEnabled: Boolean,
    headers: Map<String, String>,
    disposables: CompositeDisposable,
    networkState: Observable<Boolean>,
    onChooseFile: ((FileChooserRequest) -> Maybe<Array<Uri>>)?,
    private val overrideLoading: ((url: Uri) -> WebViewComponentModel.Result)?,
    private val context: Context
) : WebViewComponentModel {

    private sealed class Signal {
        data class Error(@StringRes val res: Int = R.string.error_generic) : Signal()
        data class Destination(val destination: WebViewDestination) : Signal()
        object LoadingFinished : Signal()
        object Loading : Signal()
    }

    override val settings = WebViewSettings(
        useCache = useCache,
        onChooseFile = onChooseFile,
        allowFileAccess = onChooseFile != null,
        javaScriptEnabled = javaScriptEnabled,
        overrideLoading = { url: String? ->
            url?.let { shouldOverrideLoadingUrl(Uri.parse(it)) }
                ?: false.also { Timber.w("WebViewComponentModel: url is null") }
        }
    )

    private val aboutToFinish = MutableLiveData(false)
    private val _showLoading = MutableLiveData<Boolean>(false)

    override val destination: LiveData<WebViewDestination> =
        // In case of `Destination` it subscribes to states from WebView.
        networkState
            .map { online ->
                when (online) {
                    true -> Signal.Destination(WebViewDestination(url, headers))
                    else -> Signal.Error(R.string.error_no_internet_connection)
                }
            }
            .distinctUntilChanged()
            .switchMap { signal ->
                when (signal) {
                    is Signal.Destination -> settings.states.map { it.asSignal() }.startWith(signal)
                    else -> Observable.just(signal)
                }
            }
            .doOnNext(::renderState)
            .ofType(Signal.Destination::class.java)
            .delayedSpinner(_showLoading)
            .map { it.destination }
            .toLiveData(disposables)
            .toSingleLiveEvent()

    override val errorMessage = MutableLiveData<String>(null)
    override val showContent = MutableLiveData<Boolean>(false)
    override val showLoading = combineLiveData(_showLoading, aboutToFinish, false)
    { loading, aboutToFinish -> loading == true || aboutToFinish == true }

    private fun renderState(signal: Signal) {
        when (signal) {
            Signal.Loading -> {
                showContent.postValue(false)
                errorMessage.postValue(null)
                _showLoading.postValue(false)
            }
            Signal.LoadingFinished -> {
                showContent.postValue(true)
                errorMessage.postValue(null)
                _showLoading.postValue(false)
            }
            is Signal.Error -> {
                showContent.postValue(false)
                errorMessage.postValue(context.getString(signal.res))
                _showLoading.postValue(false)
            }
        }
    }

    private fun shouldOverrideLoadingUrl(uri: Uri): Boolean =
        when (overrideLoading?.invoke(uri) ?: WebViewComponentModel.Result.IGNORED) {
            WebViewComponentModel.Result.CONSUMED_FINISHING -> true.also { aboutToFinish.postValue(true) }
            WebViewComponentModel.Result.CONSUMED -> true
            WebViewComponentModel.Result.IGNORED -> false
        }

    private fun WebViewSettings.State.asSignal(): Signal =
        when (this) {
            WebViewSettings.State.ERROR -> Signal.Error()
            WebViewSettings.State.STARTED -> Signal.Loading
            WebViewSettings.State.FINISHED -> Signal.LoadingFinished
        }
}

