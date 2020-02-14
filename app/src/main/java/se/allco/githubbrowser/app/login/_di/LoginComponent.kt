package se.allco.githubbrowser.app.login._di

import androidx.lifecycle.ViewModel
import dagger.Subcomponent
import se.allco.githubbrowser.app.login.LoginActivity
import se.allco.githubbrowser.common.ui.FragmentFactory
import javax.inject.Scope

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class LoginScope

@LoginScope
@Subcomponent(modules = [LoginModule::class, LoginFragmentsModule::class])
abstract class LoginComponent : ViewModel() {
    abstract fun getFragmentFactory(): FragmentFactory
    abstract fun inject(loginActivity: LoginActivity)
}
