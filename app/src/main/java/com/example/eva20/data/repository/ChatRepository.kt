package com.example.eva20.data.repository

import com.example.eva20.data.local.DatabaseManager
import com.example.eva20.network.api.ApiService
import com.example.eva20.network.models.ChatMessage
import com.example.eva20.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository {
    private val tag = "ChatRepository"

    suspend fun getMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            Logger.d(tag, "Getting all messages from local database")
            return@withContext DatabaseManager.getAllMessages()
        } catch (e: Exception) {
            Logger.e(tag, "Error getting messages", e)
            return@withContext emptyList()
        }
    }

    suspend fun sendMessage(message: ChatMessage): Boolean = withContext(Dispatchers.IO) {
        try {
            // First save locally
            DatabaseManager.addMessage(message)
            Logger.d(tag, "Message saved locally: ${message.id}")

            // Then try to send to API
            val result = ApiService.sendMessage(message)
            if (result.isSuccess) {
                // Mark as synced in local database
                DatabaseManager.markMessageAsSynced(message.id)
                Logger.d(tag, "Message sent to API and marked as synced: ${message.id}")
                return@withContext true
            } else {
                Logger.e(tag, "Failed to send message to API: ${message.id}")
                return@withContext false
            }
        } catch (e: Exception) {
            Logger.e(tag, "Error sending message", e)
            return@withContext false
        }
    }

    suspend fun saveMessage(message: ChatMessage): Boolean = withContext(Dispatchers.IO) {
        try {
            DatabaseManager.addMessage(message)
            Logger.d(tag, "Message saved locally: ${message.id}")
            return@withContext true
        } catch (e: Exception) {
            Logger.e(tag, "Error saving message", e)
            return@withContext false
        }
    }
}