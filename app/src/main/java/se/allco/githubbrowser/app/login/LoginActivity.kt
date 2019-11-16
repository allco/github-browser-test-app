package se.allco.githubbrowser.app.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.ioc.AppComponent
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.databinding.ActivityLoginBinding
import javax.inject.Inject
import javax.inject.Provider

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelProvider: Provider<LoginActivityViewModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        AppComponent.getInstance(this).inject(this)
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login).apply {
            viewModel = getViewModel(viewModelProvider)
            lifecycleOwner = this@LoginActivity
        }
    }
}
