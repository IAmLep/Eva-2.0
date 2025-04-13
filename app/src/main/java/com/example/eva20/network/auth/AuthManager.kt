package com.example.eva20.network.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.eva20.utils.Constants
import com.example.eva20.utils.Logger
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.IdTokenCredentials
import com.google.auth.oauth2.IdTokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class AuthManager {
    private val tag = "AuthManager"
    private var authToken: String? = null
    private var tokenExpirationTime: Long = 0
    private var preferences: SharedPreferences? = null
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
        preferences = appContext.getSharedPreferences(Constants.PREF_USER_SETTINGS, Context.MODE_PRIVATE)

        // Reset token on initialization for testing
        clearAuthToken()

        Logger.d(tag, "AuthManager initialized, token cleared for testing")
    }

    // Generate a JWT token using service account credentials
    suspend fun generateToken(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d(tag, "Generating token from service account credentials")

            // The exact audience for Cloud Run - MUST match exactly
            val targetAudience = "https://eva-backend-533306620971.europe-west1.run.app"
            Logger.d(tag, "Using target audience: $targetAudience")

            // Load service account credentials
            context.assets.open("service-account.json").use { inputStream ->
                // Create Google credentials
                val sourceCredentials = GoogleCredentials.fromStream(inputStream)

                // Create ID token credentials with the exact service URL as audience
                val idTokenCredentials = IdTokenCredentials.newBuilder()
                    .setIdTokenProvider(sourceCredentials as IdTokenProvider)
                    .setTargetAudience(targetAudience)
                    .build()

                // Force token refresh
                idTokenCredentials.refresh()

                // Get the ID token
                val idToken = idTokenCredentials.idToken

                if (idToken != null) {
                    authToken = idToken.tokenValue

                    // Log token for debugging (remove in production)
                    Logger.d(tag, "Generated token: ${authToken?.take(20)}...")

                    // Set expiration time (1 hour)
                    tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000

                    // Save to preferences
                    preferences?.edit {
                        putString(Constants.KEY_AUTH_TOKEN, authToken)
                        putLong(Constants.KEY_TOKEN_EXPIRATION, tokenExpirationTime)
                    }

                    Logger.d(tag, "ID Token generated successfully, expires in 1 hour")
                    return@withContext true
                } else {
                    Logger.e(tag, "Failed to get ID token")
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            Logger.e(tag, "Error generating token", e)
            return@withContext false
        }
    }

    fun isAuthenticated(): Boolean {
        val currentToken = authToken
        val isValid = currentToken != null && System.currentTimeMillis() < tokenExpirationTime
        Logger.d(tag, "Token validation check: $isValid")
        return isValid
    }

    @Suppress("unused")
    fun setAuthToken(token: String, expiresInSeconds: Int = 3600) {
        authToken = token
        tokenExpirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresInSeconds.toLong())
        preferences?.edit {
            putString(Constants.KEY_AUTH_TOKEN, token)
            putLong(Constants.KEY_TOKEN_EXPIRATION, tokenExpirationTime)
        }
        Logger.d(tag, "Auth token set, expires in $expiresInSeconds seconds")
    }

    @Suppress("unused")
    fun getAuthToken(): String? {
        return authToken
    }

    fun createAuthInterceptor(): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()

                // Skip token for specific endpoints
                val path = originalRequest.url.encodedPath
                if (path.endsWith("/health") || path.endsWith("/test")) {
                    return chain.proceed(originalRequest)
                }

                val token = authToken
                return if (token != null) {
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                    Logger.d(tag, "Added auth token to request: ${originalRequest.url}")
                    chain.proceed(newRequest)
                } else {
                    Logger.d(tag, "No auth token available for request: ${originalRequest.url}")
                    chain.proceed(originalRequest)
                }
            }
        }
    }

    @Suppress("unused")
    fun clearAuthToken() {
        authToken = null
        tokenExpirationTime = 0
        preferences?.edit {
            remove(Constants.KEY_AUTH_TOKEN)
            remove(Constants.KEY_TOKEN_EXPIRATION)
        }
        Logger.d(tag, "Auth token cleared")
    }
}