package com.example.eva20.network.api

import com.example.eva20.BuildConfig
import com.example.eva20.network.auth.AuthManager
import com.example.eva20.network.models.ChatMessage
import com.example.eva20.network.models.Memory
import com.example.eva20.utils.Constants
import com.example.eva20.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

object ApiService {
    private const val TAG = "ApiService"  // Changed to const val

    private val authManager = AuthManager()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authManager.createAuthInterceptor())
            .connectTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiClient: ApiClient by lazy {
        retrofit.create(ApiClient::class.java)
    }

    // Add this data class for authentication response
    data class AuthResponse(
        val token: String,
        val expires_in: Int? = null
    )

    // Add authentication endpoint to ApiClient interface
    interface AuthApiClient {
        @POST("auth/token")
        suspend fun getAuthToken(@Body request: AuthRequest): Response<AuthResponse>
    }

    data class AuthRequest(
        val username: String,
        val password: String,
        // For service account authentication you might use different fields
        val client_id: String? = null,
        val client_secret: String? = null,
        val grant_type: String = "password"
    )

    private val authApiClient: AuthApiClient by lazy {
        retrofit.create(AuthApiClient::class.java)
    }

    // Check if we have a valid authentication token
    fun isAuthenticated(): Boolean {
        return authManager.isAuthenticated()
    }

    // Authenticate with the backend
    suspend fun authenticate(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Attempting to authenticate with backend")

            // For this example, we're using a pre-configured service account
            // In a real app, you might get these from secure storage or BuildConfig
            val request = AuthRequest(
                username = "service-account@example.com",  // Replace with actual service account
                password = "service-account-password",     // Replace with actual password
                client_id = "eva-android-app",
                grant_type = "password"
            )

            val response = authApiClient.getAuthToken(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                authManager.setAuthToken(authResponse.token)
                Logger.d(TAG, "Authentication successful")
                true
            } else {
                Logger.e(TAG, "Authentication failed: ${response.code()} - ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error during authentication", e)
            false
        }
    }

    // Add @Suppress annotation for unused functions
    @Suppress("unused")
    suspend fun sendMessage(message: ChatMessage, useSimpleEndpoint: Boolean = false): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Sending message: ${message.text} to ${if (useSimpleEndpoint) "simple-message" else "message"} endpoint")

            val response = if (useSimpleEndpoint) {
                apiClient.sendSimpleMessage(message)
            } else {
                apiClient.sendFullMessage(message)
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    Logger.d(TAG, "Message sent successfully, received response")
                    Result.success(it)
                } ?: run {
                    Logger.e(TAG, "Response successful but body is null")
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                val errorMsg = "API call failed with code ${response.code()}: ${response.message()}"
                Logger.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception during API call", e)
            Result.failure(e)
        }
    }

    // Add memory-specific methods
    @Suppress("unused")
    suspend fun syncMemories(memories: List<Memory>): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Syncing ${memories.size} memories with backend")

            // Ensure we're authenticated
            if (!isAuthenticated()) {
                val authenticated = authenticate()
                if (!authenticated) {
                    Logger.e(TAG, "Cannot sync memories: Authentication failed")
                    return@withContext Result.failure(Exception("Authentication failed"))
                }
            }

            // Track success/failure of each memory
            var allSuccessful = true

            // For each memory, send to backend
            for (memory in memories) {
                try {
                    val response = apiClient.createMemory(memory)

                    if (!response.isSuccessful) {
                        Logger.e(TAG, "Failed to sync memory ${memory.id}: ${response.code()} - ${response.message()}")
                        allSuccessful = false
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "Error syncing memory ${memory.id}", e)
                    allSuccessful = false
                }
            }

            Result.success(allSuccessful)
        } catch (e: Exception) {
            Logger.e(TAG, "Error in syncMemories", e)
            Result.failure(e)
        }
    }

    // Health check doesn't require authentication
    suspend fun checkHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.getHealthStatus()
            response.isSuccessful
        } catch (e: Exception) {
            Logger.e(TAG, "Health check failed", e)
            false
        }
    }

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorMsg = "API call failed with code ${response.code()}: ${response.message()}"
                Logger.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception during API call", e)
            Result.failure(e)
        }
    }
}