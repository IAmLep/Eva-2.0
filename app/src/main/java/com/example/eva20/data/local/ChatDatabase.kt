package com.example.eva20.data.local

import android.content.Context
import com.example.eva20.data.local.entity.ChatMessageEntity
import com.example.eva20.network.models.ChatMessage
import com.example.eva20.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-based implementation of ChatDatabase
 */
object ChatDatabase {
    private const val TAG = "ChatDatabase"
    private var initialized = false
    private lateinit var db: AppDatabase

    fun initialize(context: Context) {
        if (initialized) return

        db = AppDatabase.getDatabase(context)

        initialized = true
        Logger.d(TAG, "ChatDatabase initialized with Room")
    }

    private fun checkInitialized() {
        if (!initialized) throw IllegalStateException("ChatDatabase not initialized")
    }

    fun getMessagesFlow(): Flow<List<ChatMessage>> {
        checkInitialized()
        return db.chatMessageDao().getAllMessagesFlow().map { entities ->
            entities.map { it.toChatMessage() }
        }
    }

    suspend fun getAllMessages(): List<ChatMessage> {
        checkInitialized()
        return db.chatMessageDao().getAllMessages().map { it.toChatMessage() }
    }

    suspend fun getUnsyncedMessages(): List<ChatMessage> {
        checkInitialized()
        return db.chatMessageDao().getUnsyncedMessages().map { it.toChatMessage() }
    }

    suspend fun getMessage(id: String): ChatMessage? {
        checkInitialized()
        return db.chatMessageDao().getMessageById(id)?.toChatMessage()
    }

    suspend fun addMessage(message: ChatMessage) {
        checkInitialized()
        val entity = ChatMessageEntity.fromChatMessage(message)
        db.chatMessageDao().insertMessage(entity)
        Logger.d(TAG, "Added message: ${message.id}")
    }

    suspend fun addOrUpdateMessage(message: ChatMessage) {
        checkInitialized()
        val entity = ChatMessageEntity.fromChatMessage(message)
        db.chatMessageDao().insertMessage(entity)
        Logger.d(TAG, "Added or updated message: ${message.id}")
    }

    suspend fun deleteMessage(id: String) {
        checkInitialized()
        db.chatMessageDao().deleteMessage(id)
        Logger.d(TAG, "Deleted message: $id")
    }

    suspend fun clearAllMessages() {
        checkInitialized()
        db.chatMessageDao().deleteAllMessages()
        Logger.d(TAG, "Cleared all messages")
    }

    suspend fun markAsSynced(id: String) {
        checkInitialized()
        db.chatMessageDao().markAsSynced(id)
        Logger.d(TAG, "Marked message as synced: $id")
    }

    suspend fun getChatMessagesWithUser(userId: String): List<ChatMessage> {
        // In a real app, you would add a query to filter by userId
        // For now, just return all messages
        return getAllMessages()
    }
}