package com.example.eva20.data.repository

import com.example.eva20.data.local.ChatDatabase
import com.example.eva20.network.api.ApiService
import com.example.eva20.network.models.ChatMessage
import com.example.eva20.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository {
    private val TAG = "ChatRepository"
    private val chatDatabase = ChatDatabase

    // Get messages from local database as a Flow
    fun getMessagesFlow(): Flow<List<ChatMessage>> {
        return chatDatabase.getMessagesFlow().flowOn(Dispatchers.IO)
    }

    // Get all messages from local database
    suspend fun getLocalMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            chatDatabase.getAllMessages()
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting local messages", e)
            emptyList()
        }
    }

    // Send a message and get the response
    suspend fun sendMessage(text: String, useSimpleEndpoint: Boolean = false): Result<ChatMessage> =
        withContext(Dispatchers.IO) {
            try {
                // Generate a unique ID for this message
                val messageId = UUID.randomUUID().toString()

                // Create the user message
                val userMessage = ChatMessage(
                    id = messageId,
                    text = text,
                    isUser = true,
                    timestamp = System.currentTimeMillis()
                )

                // Save user message to local database
                chatDatabase.addMessage(userMessage)

                // Create pending assistant message to indicate waiting for response
                val pendingMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "...",
                    isUser = false,
                    timestamp = System.currentTimeMillis(),
                    pending = true
                )
                chatDatabase.addMessage(pendingMessage)

                // Send to backend
                val result = ApiService.safeApiCall {
                    if (useSimpleEndpoint) {
                        ApiService.apiClient.sendSimpleMessage(userMessage)
                    } else {
                        ApiService.apiClient.sendFullMessage(userMessage)
                    }
                }

                if (result.isSuccess) {
                    // Remove pending message
                    chatDatabase.deleteMessage(pendingMessage.id)

                    // Add the response from the AI
                    val responseMessage = result.getOrNull()
                    if (responseMessage != null) {
                        chatDatabase.addMessage(responseMessage)
                        return@withContext Result.success(responseMessage)
                    } else {
                        val errorMessage = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = "Sorry, I couldn't process that request.",
                            isUser = false,
                            timestamp = System.currentTimeMillis(),
                            error = true
                        )
                        chatDatabase.addMessage(errorMessage)
                        return@withContext Result.failure(Exception("Response was null"))
                    }
                } else {
                    // Remove pending message
                    chatDatabase.deleteMessage(pendingMessage.id)

                    // Add error message
                    val errorMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = "Sorry, there was an error: ${result.exceptionOrNull()?.message ?: "Unknown error"}",
                        isUser = false,
                        timestamp = System.currentTimeMillis(),
                        error = true
                    )
                    chatDatabase.addMessage(errorMessage)
                    return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error sending message", e)

                // Add error message to chat
                val errorMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Sorry, there was an error: ${e.message ?: "Unknown error"}",
                    isUser = false,
                    timestamp = System.currentTimeMillis(),
                    error = true
                )
                chatDatabase.addMessage(errorMessage)

                Result.failure(e)
            }
        }

    // Get messages from backend and update local database
    suspend fun syncWithBackend(): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        try {
            // First ensure we're authenticated
            if (!ApiService.isAuthenticated()) {
                val authenticated = ApiService.authenticate()
                if (!authenticated) {
                    return@withContext Result.failure(Exception("Authentication failed"))
                }
            }

            // No specific API endpoint for getting messages history in the current setup,
            // so we'll use what we have locally
            val localMessages = chatDatabase.getAllMessages()
            Result.success(localMessages)

            // If in the future there's a backend endpoint to get message history:
            /*
            val result = ApiService.safeApiCall {
                ApiService.apiClient.getMessages()
            }

            if (result.isSuccess) {
                val messages = result.getOrNull() ?: emptyList()
                // Update local database with messages from backend
                for (message in messages) {
                    chatDatabase.addOrUpdateMessage(message)
                }
                Result.success(messages)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to sync messages"))
            }
            */
        } catch (e: Exception) {
            Logger.e(TAG, "Error syncing with backend", e)
            Result.failure(e)
        }
    }

    // Clear chat history
    suspend fun clearChatHistory(): Boolean = withContext(Dispatchers.IO) {
        try {
            chatDatabase.clearAllMessages()
            true
        } catch (e: Exception) {
            Logger.e(TAG, "Error clearing chat history", e)
            false
        }
    }
}