package com.example.eva20

import android.app.Application
import com.example.eva20.data.local.ChatDatabase
import com.example.eva20.data.local.MemoryDatabase
import com.example.eva20.network.auth.AuthManager
import com.example.eva20.utils.Logger

class EvaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure logging first for better debugging
        Logger.setDebugMode(BuildConfig.DEBUG)

        // Initialize local databases
        ChatDatabase.initialize(this)
        MemoryDatabase.initialize(this)

        // Initialize auth manager
        val authManager = AuthManager()
        authManager.initialize(this)

        Logger.i("Application", "Eva application initialized")
    }
}