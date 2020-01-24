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
import se.allco.githubbrowser.app.user.UserRepository
import se.allco.githubbrowser.app.utils.ensureUserLoggedIn
import se.allco.githubbrowser.databinding.ActivityMainBinding
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        AppComponent.getInstance(this).inject(this)
        super.onCreate(savedInstanceState)
        ensureUserLoggedIn(userRepository) {
            val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            val navController = findNavController(R.id.nav_host_fragment)
            val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.navigation_repos)
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            binding.navView.setupWithNavController(navController)
        }
    }
}
