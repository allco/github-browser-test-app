package se.allco.githubbrowser.app.login.di

import androidx.lifecycle.ViewModel
import dagger.Subcomponent
import se.allco.githubbrowser.app.login.LoginActivity
import se.allco.githubbrowser.common.FragmentFactory

@Subcomponent(modules = [LoginFragmentsModule::class])
abstract class LoginComponent : ViewModel() {
    abstract fun getFragmentFactory(): FragmentFactory
    abstract fun inject(loginActivity: LoginActivity)
}
