package se.allco.githubbrowser.app.login.autologin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.utils.ObserverNonNull
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.databinding.LoginAutoFragmentBinding
import javax.inject.Inject
import javax.inject.Provider

class AutoLoginFragment @Inject constructor(
    private val viewModelProvider: Provider<AutoLoginViewModel>,
    private val listener: Listener
) : Fragment() {

    interface Listener {
        fun onAutoLoginResult(user: User)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        LoginAutoFragmentBinding.inflate(inflater, container, false)
            .also { binding ->
                val viewModel = getViewModel(viewModelProvider)
                binding.viewModel = viewModel
                binding.lifecycleOwner = viewLifecycleOwner
                viewModel.result.observe(viewLifecycleOwner, ObserverNonNull(::onLoginResult))
            }
            .root

    private fun onLoginResult(user: User) {
        listener.onAutoLoginResult(user)
    }
}
