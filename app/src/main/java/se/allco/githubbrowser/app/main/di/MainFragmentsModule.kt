package se.allco.githubbrowser.app.main.di

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import se.allco.githubbrowser.app.main.account.AccountFragment
import se.allco.githubbrowser.app.main.repos.ReposFragment
import se.allco.githubbrowser.common.FragmentKey

@Module
abstract class MainFragmentsModule {

    @Binds
    @IntoMap
    @FragmentKey(ReposFragment::class)
    abstract fun bindReposFragment(fragment: ReposFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(AccountFragment::class)
    abstract fun bindAccountFragment(fragment: AccountFragment): Fragment
}
