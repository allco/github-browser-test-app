package se.allco.githubbrowser.app.login.github

import android.net.Uri
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.SingleSubject
import se.allco.githubbrowser.BuildConfig
import se.allco.githubbrowser.app.user.GithubToken
import se.allco.githubbrowser.common.ui.components.WebViewComponentModel
import se.allco.githubbrowser.common.utils.plusAssign
import se.allco.githubbrowser.common.utils.subscribeSafely
import java.util.UUID
import javax.inject.Inject

class GithubLoginWebViewModel @Inject constructor(
    private val repository: GithubLoginWebRepository,
    private val webViewComponentViewModelBuilder: WebViewComponentModel.Builder
) {

    private fun createdUri(requestId: String) =
        Uri.Builder().apply {
            scheme("https")
                .authority("github.com")
                .appendPath("login")
                .appendPath("oauth")
                .appendPath("authorize")
                .appendQueryParameter("state", requestId)
                .appendQueryParameter("client_id", BuildConfig.GITHUB_CLIENT_ID)
        }.build().toString()

    inner class UrlNavigationHandler(
        private val requestId: String,
        private val disposables: CompositeDisposable,
        private val callback: SingleSubject<GithubToken>
    ) {

        private fun extractCode(redirectUrl: Uri): String? {
            val state = redirectUrl.getQueryParameter("state")
            val code = redirectUrl.getQueryParameter("code")
            return when {
                state != requestId -> {
                    callback.onError(RuntimeException("GithubLoginWebViewModel state != requestId"))
                    null
                }
                code == null -> {
                    callback.onError(RuntimeException("GithubLoginWebViewModel code == null"))
                    null
                }
                else -> code
            }
        }

        private fun processCode(code: String) {
            disposables += repository.fetchAccessToken(code).subscribeSafely(callback)
        }

        val onNavigateToUrl = { redirectUrl: Uri ->
            when (redirectUrl.scheme) {
                BuildConfig.APP_SCHEMA -> {
                    extractCode(redirectUrl)?.let(::processCode)
                    WebViewComponentModel.Result.CONSUMED_FINISHING
                }
                else -> WebViewComponentModel.Result.IGNORED
            }
        }
    }

    fun createWebViewModel(
        disposables: CompositeDisposable,
        callback: SingleSubject<GithubToken>
    ): WebViewComponentModel =
        UUID.randomUUID().toString().let { requestId ->
            webViewComponentViewModelBuilder
                .apply { overrideLoading = UrlNavigationHandler(requestId, disposables, callback).onNavigateToUrl }
                .build(
                    createdUri(requestId),
                    mapOf("Accept" to "application/vnd.github.machine-man-preview+json"),
                    disposables
                )
        }
}
