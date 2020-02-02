package se.allco.githubbrowser.app.login.autologin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.ui.findListenerOrThrow
import se.allco.githubbrowser.common.utils.ObserverNonNull
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.databinding.LoginAutoFragmentBinding
import javax.inject.Inject
import javax.inject.Provider

class AutoLoginFragment @Inject constructor(
    private val viewModelProvider: Provider<AutoLoginViewModel>
) : Fragment() {

    interface Listener {
        fun onAutoLoginResult(user: User)
    }

    private lateinit var listener: Listener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = findListenerOrThrow()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        LoginAutoFragmentBinding.inflate(inflater, container, false)
            .also { binding ->
                val viewModel = getViewModel()
                binding.viewModel = viewModel
                binding.lifecycleOwner = viewLifecycleOwner
                viewModel.result.observe(this, ObserverNonNull(::onLoginResult))
            }
            .root

    private fun onLoginResult(user: User) {
        listener.onAutoLoginResult(user)
    }

    private fun getViewModel(): AutoLoginViewModel = getViewModel(viewModelProvider)
}
