package se.allco.githubbrowser.app.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.disposables.CompositeDisposable
import se.allco.githubbrowser.app.uicomponents.webview.WebViewComponentModel
import javax.inject.Inject

class LoginActivityViewModel @Inject constructor(
    application: Application,
    webViewComponentViewModelBuilder: WebViewComponentModel.Builder
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    val webViewComponentViewModel =
        webViewComponentViewModelBuilder
            .build("google.com", disposables)

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
