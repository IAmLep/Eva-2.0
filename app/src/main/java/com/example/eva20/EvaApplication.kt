package com.example.eva20

import android.app.Application
import com.example.eva20.data.local.DatabaseManager
import com.example.eva20.network.auth.AuthManager
import com.example.eva20.network.api.ApiService
import com.example.eva20.utils.Logger

class EvaApplication : Application() {

    private lateinit var authManager: AuthManager

    override fun onCreate() {
        super.onCreate()
        // Initialize the Database Manager
        DatabaseManager.initialize(this)

        // Configure logging first for better debugging
        Logger.setDebugMode(BuildConfig.DEBUG)
        Logger.i("Application", "Eva application starting...")

        // Initialize authentication manager
        authManager = AuthManager()
        authManager.initialize(this)

        // Initialize local databases
        try {
            DatabaseManager.initialize(this)
            Logger.i("Application", "Chat database initialized")
        } catch (e: Exception) {
            Logger.e("Application", "Failed to initialize chat database", e)
        }

        try {
            DatabaseManager.initialize(this)
            Logger.i("Application", "Memory database initialized")
        } catch (e: Exception) {
            Logger.e("Application", "Failed to initialize memory database", e)
        }

        // Log application initialized
        Logger.i("Application", "Eva application initialized")
    }

    // Make auth manager accessible throughout the application
    fun getAuthManager(): AuthManager {
        return authManager
    }
}