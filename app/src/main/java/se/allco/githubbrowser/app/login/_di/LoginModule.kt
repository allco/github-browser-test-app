package se.allco.githubbrowser.app.login._di

import dagger.Module
import dagger.Provides
import se.allco.githubbrowser.app.login.LoginActivityViewModel
import se.allco.githubbrowser.app.login.LoginRepository
import se.allco.githubbrowser.app.login.autologin.AutoLoginFragment
import se.allco.githubbrowser.app.login.autologin.AutoLoginRepository
import se.allco.githubbrowser.app.login.manuallogin.GithubLoginWebViewModel
import se.allco.githubbrowser.app.login.manuallogin.GithubLoginWebViewModelImpl
import se.allco.githubbrowser.app.login.manuallogin.ManualLoginFragment
import se.allco.githubbrowser.app.login.manuallogin.ManualLoginRepository

@Module
class LoginModule {

    @Provides
    fun provideAutoLoginFragmentListener(impl: LoginActivityViewModel): AutoLoginFragment.Listener = impl

    @Provides
    fun provideManualLoginFragmentListener(impl: LoginActivityViewModel): ManualLoginFragment.Listener = impl

    @Provides
    fun providesAutoLoginRepository(impl: LoginRepository): AutoLoginRepository = impl

    @Provides
    fun providesManualLoginRepository(impl: LoginRepository): ManualLoginRepository = impl

    @Provides
    fun providesGithubLoginWebViewModel(impl: GithubLoginWebViewModelImpl): GithubLoginWebViewModel = impl
}
