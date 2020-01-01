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
import se.allco.githubbrowser.common.utils.asOptional
import se.allco.githubbrowser.common.utils.combine
import se.allco.githubbrowser.common.utils.distinctUntilChanged
import se.allco.githubbrowser.common.utils.map
import se.allco.githubbrowser.common.utils.toLiveData
import timber.log.Timber
import toSingleLiveEvent

class WebViewComponentModelImpl(
    url: String,
    context: Context,
    headers: Map<String, String>,
    disposables: CompositeDisposable,
    networkState: Observable<Boolean>,
    onChooseFile: ((FileChooserRequest) -> Maybe<Array<Uri>>)?,
    private val overrideLoading: ((url: Uri) -> WebViewComponentModel.Result)?
) : WebViewComponentModel {

    sealed class State {
        data class Error(@StringRes val res: Int = R.string.error_generic) : State()
        data class Destination(val destination: WebViewDestination) : State()
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
            .map { connectivity ->
                when (connectivity) {
                    true -> State.Destination(
                        WebViewDestination(url, headers)
                    )
                    else -> State.Error(
                        R.string.error_no_internet_connection
                    )
                }
            }
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
        when (overrideLoading?.invoke(uri) ?: WebViewComponentModel.Result.IGNORED) {
            WebViewComponentModel.Result.CONSUMED_FINISHING -> true.also { aboutToFinish.postValue(true) }
            WebViewComponentModel.Result.CONSUMED -> true
            WebViewComponentModel.Result.IGNORED -> false
        }

    override val destination: LiveData<WebViewDestination> =
        state
            .ofType(State.Destination::class.java)
            .map { it.destination }
            .toLiveData(disposables)
            .toSingleLiveEvent()

    override val showContent: LiveData<Boolean> =
        state
            .map { it is State.Finished }
            .toLiveData(disposables)
            .distinctUntilChanged()

    override val showLoading: LiveData<Boolean> =
        state
            .map { it is State.Loading || it is State.Destination }
            .distinctUntilChanged()
            .toLiveData(disposables)
            .combine(aboutToFinish, false) { pageIsLoading, finishing ->
                pageIsLoading == true || finishing == true
            }
            .distinctUntilChanged()

    override val errorMessage: LiveData<String> =
        state
            .map { cmd -> (cmd as? State.Error)?.let { context.getString(it.res) }.asOptional() }
            .toLiveData(disposables)
            .map { it?.asNullable() }
            .distinctUntilChanged()
}

private fun WebViewSettings.State.asState(): WebViewComponentModelImpl.State =
    when (this) {
        WebViewSettings.State.ERROR -> WebViewComponentModelImpl.State.Error()
        WebViewSettings.State.STARTED -> WebViewComponentModelImpl.State.Loading
        WebViewSettings.State.FINISHED -> WebViewComponentModelImpl.State.Finished
    }
