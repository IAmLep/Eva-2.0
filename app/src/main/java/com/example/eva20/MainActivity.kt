package com.example.eva20

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.eva20.databinding.ActivityMainBinding
import com.example.eva20.network.api.ApiService
import com.example.eva20.utils.Logger
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_chat, R.id.nav_memory
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Make sure Chat is the default selected item
        if (savedInstanceState == null) {
            navController.navigate(R.id.nav_chat)
            navView.setCheckedItem(R.id.nav_chat)
        }

        // Listen for navigation changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Logger.d(tag, "Navigated to: ${destination.label}")

            // Hide drawer if navigating to call fragment (since it's an overlay)
            if (destination.id == R.id.nav_call) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }

        // Check backend health
        checkBackendHealth()

        // Log application startup with current time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentTime = dateFormat.format(Date())
        Logger.i(tag, "Main activity created at $currentTime by user: IAmLep")
    }

    private fun checkBackendHealth() {
        lifecycleScope.launch {
            try {
                val isHealthy = ApiService.checkHealth()
                if (!isHealthy) {
                    Toast.makeText(
                        this@MainActivity,
                        "Warning: Backend connectivity issues detected",
                        Toast.LENGTH_LONG
                    ).show()
                    Logger.w(tag, "Backend health check failed")
                } else {
                    Logger.d(tag, "Backend health check passed")
                }
            } catch (e: Exception) {
                Logger.e(tag, "Error checking backend health", e)
                Toast.makeText(
                    this@MainActivity,
                    "Warning: Couldn't connect to the backend",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // If on call fragment, navigate back to chat
            if (navController.currentDestination?.id == R.id.nav_call) {
                navController.navigateUp()
            } else if (navController.currentDestination?.id != R.id.nav_chat) {
                // If not on chat fragment, navigate to chat
                navController.navigate(R.id.nav_chat)
            } else {
                // If on chat fragment, proceed with normal back button behavior
                super.onBackPressed()
            }
        }
    }
}