package com.example.eva20.network.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.eva20.utils.Constants
import com.example.eva20.utils.Logger
import okhttp3.Interceptor
import okhttp3.Response

class AuthManager {
    private val TAG = "AuthManager"
    private var authToken: String? = null
    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(Constants.PREF_USER_SETTINGS, Context.MODE_PRIVATE)
        // Restore token if available
        authToken = preferences?.getString(Constants.KEY_AUTH_TOKEN, null)
        Logger.d(TAG, "AuthManager initialized, token ${if (authToken == null) "not found" else "restored"}")
    }

    fun getAuthToken(): String? {
        return authToken
    }

    fun setAuthToken(token: String) {
        authToken = token
        preferences?.edit()?.putString(Constants.KEY_AUTH_TOKEN, token)?.apply()
        Logger.d(TAG, "Auth token updated and saved")
    }

    fun clearAuthToken() {
        authToken = null
        preferences?.edit()?.remove(Constants.KEY_AUTH_TOKEN)?.apply()
        Logger.d(TAG, "Auth token cleared")
    }

    fun isAuthenticated(): Boolean {
        return !authToken.isNullOrEmpty()
    }

    // Create an OkHttp interceptor that adds the auth token to all requests
    fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()

            // Only add the auth header if we have a token
            val request = if (!authToken.isNullOrEmpty()) {
                Logger.d(TAG, "Adding auth token to request: ${original.url}")
                original.newBuilder()
                    .header("Authorization", "Bearer $authToken")
                    .build()
            } else {
                Logger.d(TAG, "No auth token available for request: ${original.url}")
                original
            }

            chain.proceed(request)
        }
    }
}