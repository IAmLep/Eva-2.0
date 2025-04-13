package com.example.eva20

import android.app.Application
import com.example.eva20.data.local.DatabaseManager
import com.example.eva20.network.api.ApiService
import com.example.eva20.network.auth.AuthManager
import com.example.eva20.utils.Logger

class EvaApplication : Application() {

    private lateinit var authManager: AuthManager

    override fun onCreate() {
        super.onCreate()
        // Configure logging first for better debugging
        Logger.setDebugMode(BuildConfig.DEBUG)
        Logger.i("Application", "Eva application starting...")

        // Initialize authentication manager
        authManager = AuthManager()
        authManager.initialize(this)  // Must be done before other initializations

        // Initialize API Service with application context
        ApiService.initialize(this)
        Logger.i("Application", "API Service initialized")

        // Initialize local databases
        try {
            DatabaseManager.initialize(this)
            Logger.i("Application", "Database initialized")
        } catch (e: Exception) {
            Logger.e("Application", "Failed to initialize database", e)
        }

        Logger.i("Application", "Eva application initialized")
    }

    // Make auth manager accessible throughout the application
    @Suppress("unused")
    fun getAuthManager(): AuthManager {
        return authManager
    }
}