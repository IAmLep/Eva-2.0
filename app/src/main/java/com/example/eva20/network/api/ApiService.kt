package com.example.eva20.network.api

import android.content.Context
import com.example.eva20.BuildConfig
import com.example.eva20.network.auth.AuthManager
import com.example.eva20.network.models.ChatMessage
import com.example.eva20.network.models.Memory
import com.example.eva20.network.models.SimpleMessageRequest
import com.example.eva20.network.models.SimpleMessageResponse
import com.example.eva20.utils.Constants
import com.example.eva20.utils.Logger
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

object ApiService {
    private const val TAG = "ApiService"

    private val authManager = AuthManager()
    private var isInitialized = false
    private lateinit var appContext: Context

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

    // Custom Gson configuration for handling date format issues
    private val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(Date::class.java, DateDeserializer())
            .create()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))  // Use custom gson
            .build()
    }

    val apiClient: ApiClient by lazy {
        retrofit.create(ApiClient::class.java)
    }

    // Initialize with application context
    fun initialize(context: Context) {
        if (isInitialized) return

        appContext = context.applicationContext
        authManager.initialize(appContext)
        isInitialized = true
        Logger.d(TAG, "ApiService initialized successfully")
    }

    // Check if we have a valid authentication token
    fun isAuthenticated(): Boolean {
        return authManager.isAuthenticated()
    }

    // Authenticate with the backend using Google service account
    suspend fun authenticate(): Boolean = withContext(Dispatchers.IO) {
        try {
            Logger.d(TAG, "Attempting to authenticate with Cloud Run service")

            // During testing, always generate a fresh token
            val success = authManager.generateToken(appContext)

            if (success) {
                Logger.d(TAG, "Authentication successful")
                return@withContext true
            } else {
                Logger.e(TAG, "Failed to generate authentication token")
                return@withContext false
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error during authentication", e)
            return@withContext false
        }
    }

    suspend fun sendMessage(message: ChatMessage, useSimpleEndpoint: Boolean = true): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            // Check authentication before sending
            if (!isAuthenticated()) {
                Logger.d(TAG, "Not authenticated, attempting to authenticate")
                val authenticated = authenticate()
                if (!authenticated) {
                    Logger.e(TAG, "Failed to authenticate, cannot send message")
                    return@withContext Result.failure(Exception("Authentication failed"))
                }
            }

            Logger.d(TAG, "Sending message: ${message.text} to ${if (useSimpleEndpoint) "simple-message" else "message"} endpoint")

            if (useSimpleEndpoint) {
                // Convert ChatMessage to SimpleMessageRequest
                val simpleRequest = SimpleMessageRequest(
                    message = message.text,
                    context = mapOf(
                        "user_id" to message.userId,
                        "timestamp" to message.timestamp.toString()
                    )
                )

                // Call the simple endpoint
                val response = apiClient.sendSimpleMessage(simpleRequest)

                if (response.isSuccessful) {
                    response.body()?.let { simpleResponse ->
                        // Convert SimpleMessageResponse back to ChatMessage
                        val responseMessage = ChatMessage(
                            id = System.currentTimeMillis().toString(),
                            text = simpleResponse.response,
                            isFromUser = false,
                            timestamp = simpleResponse.timestamp.time,
                            userId = "EVA-BOT"
                        )
                        Logger.d(TAG, "Simple message sent successfully, received response")
                        return@withContext Result.success(responseMessage)
                    } ?: run {
                        Logger.e(TAG, "Response successful but body is null")
                        return@withContext Result.failure(Exception("Response body is null"))
                    }
                } else {
                    val errorMsg = "API call failed with code ${response.code()}: ${response.message()}"
                    Logger.e(TAG, errorMsg)
                    return@withContext Result.failure(Exception(errorMsg))
                }
            } else {
                // Use original endpoint (probably won't work with current backend)
                val response = apiClient.sendFullMessage(message)

                if (response.isSuccessful) {
                    response.body()?.let {
                        Logger.d(TAG, "Full message sent successfully, received response")
                        return@withContext Result.success(it)
                    } ?: run {
                        Logger.e(TAG, "Response successful but body is null")
                        return@withContext Result.failure(Exception("Response body is null"))
                    }
                } else {
                    val errorMsg = "API call failed with code ${response.code()}: ${response.message()}"
                    Logger.e(TAG, errorMsg)
                    return@withContext Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception during API call", e)
            return@withContext Result.failure(e)
        }
    }

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

            return@withContext Result.success(allSuccessful)
        } catch (e: Exception) {
            Logger.e(TAG, "Error in syncMemories", e)
            return@withContext Result.failure(e)
        }
    }

    // Health check doesn't require authentication
    suspend fun checkHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.getHealthStatus()
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            Logger.e(TAG, "Health check failed", e)
            return@withContext false
        }
    }

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> = withContext(Dispatchers.IO) {
        try {
            // Check authentication before making the API call
            if (!isAuthenticated()) {
                authenticate()
            }

            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let {
                    return@withContext Result.success(it)
                } ?: return@withContext Result.failure(Exception("Response body is null"))
            } else {
                val errorMsg = "API call failed with code ${response.code()}: ${response.message()}"
                Logger.e(TAG, errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception during API call", e)
            return@withContext Result.failure(e)
        }
    }
}