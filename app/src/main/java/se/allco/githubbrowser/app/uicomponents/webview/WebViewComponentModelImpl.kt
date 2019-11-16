package se.allco.githubbrowser.app.uicomponents.webview

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
import se.allco.githubbrowser.common.ui.toSingleLiveEvent
import se.allco.githubbrowser.common.utils.asOptional
import se.allco.githubbrowser.common.utils.combine
import se.allco.githubbrowser.common.utils.map
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber

class WebViewComponentModelImpl(
    context: Context,
    disposables: CompositeDisposable,
    networkState: Observable<Boolean>,
    onChooseFile: ((FileChooserRequest) -> Maybe<Array<Uri>>)?,
    private val overrideLoading: ((url: Uri) -> WebViewComponentModel.Result)?,
    private val url: String
) : WebViewComponentModel {

    sealed class State {
        class Error(@StringRes val res: Int = R.string.error_generic) : State()
        class Destination(val dst: WebViewDestination) : State()
        object Loading : State()
        object Finished : State()
    }

    override val settings = WebViewSettings(
        useCache = false,
        allowFileAccess = onChooseFile != null,
        onChooseFile = onChooseFile,
        overrideLoading = { url: String? ->
            url?.let { shouldOverrideLoadingUrl(Uri.parse(it)) }
                ?: false.also { Timber.w("WebViewComponentModel: url is null") }
        }
    )

    /**
     * Emits `State.Error` and/or `State.Destination`
     * Can emmit multiple `State.Error` before first `State.Destination`
     * First it reads connectivity status.
     * if its `true` then creates `State.Destination`, or `State.Error` otherwise.
     * It completes after first emission of `State.Destination`
     */
    private val destinationState: Observable<State> =
        networkState
            .map { onConnectivityChanged(it) }
            .startWith(State.Loading)
            .distinctUntilChanged()

    /**
     * Emits any kind of `State`
     * In case of `Destination` it subscribes to states from WebView.
     * all other state are just being passed further
     */
    private val state: Observable<State> =
        destinationState
            .switchMap { state ->
                when (state) {
                    is State.Destination -> settings.states.map { it.asState() }.startWith(state)
                    else -> Observable.just(state)
                }
            }
            .replay(1)
            .refCount()

    private val aboutToFinish = MutableLiveData(false)

    private fun shouldOverrideLoadingUrl(uri: Uri): Boolean =
        when (overrideLoading?.invoke(uri) ?: WebViewComponentModel.Result.IGNORE) {
            WebViewComponentModel.Result.CONSUME_AND_FINISH -> true.also { aboutToFinish.postValue(true) }
            WebViewComponentModel.Result.CONSUME -> true
            WebViewComponentModel.Result.IGNORE -> false
        }

    override val destination: LiveData<WebViewDestination> =
        state
            .ofType(State.Destination::class.java)
            .map { it.dst }
            .toLiveData(disposables)
            .toSingleLiveEvent()

    override val showContent: LiveData<Boolean> =
        state
            .map { it is State.Finished }
            .toLiveData(disposables)

    override val showLoading: LiveData<Boolean> =
        state
            .map { it is State.Loading || it is State.Destination }
            .distinctUntilChanged()
            .toLiveData(disposables)
            .combine(aboutToFinish, false) { pageIsLoading, finishing ->
                pageIsLoading == true || finishing == true
            }

    override val errorMessage: LiveData<String> =
        state
            .map { cmd -> (cmd as? State.Error)?.let { context.getString(it.res) }.asOptional() }
            .toLiveData(disposables)
            .map { it?.asNullable() }

    private fun onConnectivityChanged(connectivity: Boolean): State =
        when (connectivity) {
            true -> State.Destination(WebViewDestination(url))
            else -> State.Error(R.string.error_no_internet_connection)
        }
}

private fun WebViewSettings.State.asState(): WebViewComponentModelImpl.State =
    when (this) {
        WebViewSettings.State.ERROR -> WebViewComponentModelImpl.State.Error()
        WebViewSettings.State.STARTED -> WebViewComponentModelImpl.State.Loading
        WebViewSettings.State.FINISHED -> WebViewComponentModelImpl.State.Finished
    }