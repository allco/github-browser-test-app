package se.allco.githubbrowser.app.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import se.allco.githubbrowser.R
import se.allco.githubbrowser.app.di.AppComponent
import se.allco.githubbrowser.app.main.di.MainComponent
import se.allco.githubbrowser.app.user.UserRepository
import se.allco.githubbrowser.app.user.di.UserComponentHolder
import se.allco.githubbrowser.app.utils.ensureUserLoggedIn
import se.allco.githubbrowser.common.utils.getViewModel
import se.allco.githubbrowser.databinding.ActivityMainBinding
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var userComponentHolder: UserComponentHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        AppComponent.getInstance(this).inject(this)
        supportFragmentManager.fragmentFactory = getMainComponent().getFragmentFactory()
        super.onCreate(savedInstanceState)
        ensureUserLoggedIn(userRepository) {
            val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            val navController = findNavController(R.id.nav_host_fragment)
            val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_repos, R.id.navigation_account)
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            binding.navView.setupWithNavController(navController)
        }
    }

    private fun getMainComponent(): MainComponent = getViewModel {
        userComponentHolder
            .getUserComponent()
            .createMainComponent()
    }
}
