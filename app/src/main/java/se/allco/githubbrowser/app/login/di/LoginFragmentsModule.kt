package se.allco.githubbrowser.app.login.di

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import se.allco.githubbrowser.app.login.autologin.AutoLoginFragment
import se.allco.githubbrowser.common.ui.FragmentKey

@Module
abstract class LoginFragmentsModule {
    @Binds
    @IntoMap
    @FragmentKey(AutoLoginFragment::class)
    abstract fun bindAutoLoginFragment(fragment: AutoLoginFragment): Fragment
}
