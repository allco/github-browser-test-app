package se.allco.githubbrowser.common.ui.databinding.webview.webclient

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class WebClient : WebViewClient() {

    enum class State { STARTED, FINISHED, ERROR }
    enum class LoadingCommand { CONTINUE_LOADING, BREAK_LOADING }

    private val _statesSubject: PublishSubject<State> = PublishSubject.create()
    val states: Observable<State>
        get() = _statesSubject

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        postState(State.STARTED)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        postState(State.FINISHED)
    }

    protected fun postState(state: State) {
        _statesSubject.onNext(state)
    }
}

