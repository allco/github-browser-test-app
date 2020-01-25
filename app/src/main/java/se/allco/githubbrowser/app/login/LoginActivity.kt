package se.allco.githubbrowser.app.login

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.databinding.DataBindingUtil
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.di.AppComponent
import se.allco.githubbrowser.app.main.MainActivity
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.utils.ObserverNonNull
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.databinding.ActivityLoginBinding
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val ARG_CALLBACK = "ARG_CALLBACK"

        fun readCallbackIntent(intent: Intent): PendingIntent? =
            intent.getParcelableExtra(ARG_CALLBACK)

        fun createIntent(activity: Activity): Intent {
            // create an PendingIntent based on the same intent which `activity` was started with.
            val callback = TaskStackBuilder
                .create(activity)
                .addNextIntentWithParentStack(activity.intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            return Intent(activity, LoginActivity::class.java)
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(ARG_CALLBACK, callback)
                }
        }
    }

    @Inject
    lateinit var viewModelProvider: Provider<LoginActivityViewModel>

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        AppComponent.getInstance(this).inject(this)
        super.onCreate(savedInstanceState)
        val viewModel = getViewModel(viewModelProvider)
        val binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
        binding.lifecycleOwner = this@LoginActivity
        binding.viewModel = viewModel
        webView = binding.include.webView
        viewModel.loggedInUser.observe(this@LoginActivity, ObserverNonNull(::onUserLoggedIn))
    }

    private fun onUserLoggedIn(@Suppress("UNUSED_PARAMETER") user: User.Valid) {
        Timber.v("onUserLoggedIn() called  with: user = [$user]")
        readCallbackIntent(intent)?.send() ?: startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }

    override fun onBackPressed() {
        webView.takeIf { it.canGoBack() }
            ?.goBack()
            ?: super.onBackPressed()
    }
}
