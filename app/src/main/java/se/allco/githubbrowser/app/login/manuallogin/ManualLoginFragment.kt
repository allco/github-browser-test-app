package se.allco.githubbrowser.app.login.manuallogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.user.User
import se.allco.githubbrowser.common.ui.overrideOnBackPress
import se.allco.githubbrowser.common.utils.ObserverNonNull
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.databinding.LoginManualFragmentBinding
import javax.inject.Inject
import javax.inject.Provider

class ManualLoginFragment @Inject constructor(
    private val viewModelProvider: Provider<ManualLoginViewModel>,
    private val listener: Listener
) : Fragment() {

    interface Listener {
        fun onManualLoginResult(user: User.Valid)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        LoginManualFragmentBinding
            .inflate(inflater, container, false)
            .also { initViews(it) }
            .root

    private fun initViews(binding: LoginManualFragmentBinding) {
        val navController = findNavController()
        val viewModel = getViewModel(viewModelProvider)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(setOf(R.id.navigation_login_manual))
        )
        viewModel.result.observe(this, ObserverNonNull(::onLoginResult))
        overrideOnBackPress {
            binding.webView.takeIf { it.canGoBack() }?.goBack() ?: kotlin.run {
                remove()
                requireActivity().onBackPressed()
            }
        }
    }

    private fun onLoginResult(user: User.Valid) {
        listener.onManualLoginResult(user)
    }
}
