package com.example.eva20.data.local

import android.content.Context
import com.example.eva20.data.local.entity.ChatMessageEntity
import com.example.eva20.data.local.entity.MemoryEntity
import com.example.eva20.network.models.ChatMessage
import com.example.eva20.network.models.Memory
import com.example.eva20.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DatabaseManager {
    private const val TAG = "DatabaseManager"
    private var database: AppDatabase? = null

    // Current time and user information
    private val currentTime: String
        get() {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            return dateFormat.format(Date())
        }

    private const val CURRENTUSER: String = "IAmLep"

    fun initialize(context: Context) {
        if (database == null) {
            database = AppDatabase.getDatabase(context)
            Logger.d(TAG, "Database initialized at $currentTime by $CURRENTUSER")
        }
    }

    // Chat Message Operations
    suspend fun getAllMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val entities = dao.getAllMessages()
            return@withContext entities.map { ChatMessageEntity.toChatMessage(it) }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting all chat messages", e)
            return@withContext emptyList()
        }
    }

    @Suppress("unused")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>> {
        val dao = database?.chatMessageDao()
            ?: throw IllegalStateException("Database not initialized")

        return dao.getAllMessagesFlow()
            .map { entities -> entities.map { ChatMessageEntity.toChatMessage(it) } }
    }

    @Suppress("unused")
    suspend fun getMessageById(messageId: String): ChatMessage? = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = dao.getMessageById(messageId)
            return@withContext entity?.let { ChatMessageEntity.toChatMessage(it) }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting message by ID: $messageId", e)
            return@withContext null
        }
    }

    suspend fun addMessage(message: ChatMessage): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = ChatMessageEntity.fromChatMessage(message)
            val insertResult = dao.insertMessage(entity)
            Logger.d(TAG, "Chat message added with ID: ${message.id} by $CURRENTUSER at $currentTime")
            return@withContext insertResult > 0
        } catch (e: Exception) {
            Logger.e(TAG, "Error adding chat message", e)
            return@withContext false
        }
    }

    @Suppress("unused")
    suspend fun updateMessage(message: ChatMessage): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = ChatMessageEntity.fromChatMessage(message)
            val updateResult = dao.updateMessage(entity)
            Logger.d(TAG, "Chat message updated with ID: ${message.id} by $CURRENTUSER at $currentTime")
            return@withContext updateResult > 0
        } catch (e: Exception) {
            Logger.e(TAG, "Error updating chat message", e)
            return@withContext false
        }
    }

    @Suppress("unused")
    suspend fun deleteMessage(messageId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val deleteResult = dao.deleteMessage(messageId)
            Logger.d(TAG, "Chat message deleted with ID: $messageId by $CURRENTUSER at $currentTime")
            return@withContext deleteResult > 0
        } catch (e: Exception) {
            Logger.e(TAG, "Error deleting chat message", e)
            return@withContext false
        }
    }

    suspend fun markMessageAsSynced(messageId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val updateResult = dao.markAsSynced(messageId)
            Logger.d(TAG, "Chat message marked as synced: $messageId by $CURRENTUSER at $currentTime")
            return@withContext updateResult > 0
        } catch (e: Exception) {
            Logger.e(TAG, "Error marking chat message as synced", e)
            return@withContext false
        }
    }

    @Suppress("unused")
    suspend fun getUnsyncedMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val entities = dao.getUnsyncedMessages()
            return@withContext entities.map { ChatMessageEntity.toChatMessage(it) }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting unsynced chat messages", e)
            return@withContext emptyList()
        }
    }

    // Memory Operations
    suspend fun getAllMemories(): List<Memory> = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entities = dao.getAllMemories()
            return@withContext entities.map { MemoryEntity.toMemory(it) }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting all memories", e)
            return@withContext emptyList()
        }
    }

    @Suppress("unused")
    fun getAllMemoriesFlow(): Flow<List<Memory>> {
        val dao = database?.memoryDao()
            ?: throw IllegalStateException("Database not initialized")

        return dao.getAllMemoriesFlow()
            .map { entities -> entities.map { MemoryEntity.toMemory(it) } }
    }

    @Suppress("unused")
    suspend fun getMemoryById(memoryId: String): Memory? = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = dao.getMemoryById(memoryId)
            return@withContext entity?.let { MemoryEntity.toMemory(it) }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting memory by ID: $memoryId", e)
            return@withContext null
        }
    }

    suspend fun addMemory(memory: Memory): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = MemoryEntity.fromMemory(memory)
            val insertResult = dao.insertMemory(entity)
            Logger.d(TAG, "Memory added with ID: ${memory.id} by $CURRENTUSER at $currentTime")
            return@withContext insertResult > 0
        } catch (e: Exception) {
            Logger.e(TAG, "Error adding memory", e)
            return@withContext false
        }
    }

    @Suppress("unused")
    suspend fun updateMemory(memory: Memory): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = MemoryEntity.fromMemory(memory)
            val updateResult = dao.updateMemory(entity)
            Logger.d(TAG, "Memory updated with ID: ${memory.id} by $CURRENTUSER at $currentTime")
            return@withContext updateResult > 0
        } catch (e: Exception) {
            Logger.e(TAG, "Error updating memory", e)
            return@withContext false
        }
    }

    suspend fun deleteMemory(memoryId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val deleteResult = dao.deleteMemory(memoryId)
            Logger.d(TAG, "Memory deleted with ID: $memoryId by $CURRENTUSER at $currentTime")
            return@withContext deleteResult > 0
        } catch (e: Exception) {
            Logger.e(TAG, "Error deleting memory", e)
            return@withContext false
        }
    }

    suspend fun markMemoryAsSynced(memoryId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val updateResult = dao.markAsSynced(memoryId)
            Logger.d(TAG, "Memory marked as synced: $memoryId by $CURRENTUSER at $currentTime")
            return@withContext updateResult > 0
        } catch (e: Exception) {
            Logger.e(TAG, "Error marking memory as synced", e)
            return@withContext false
        }
    }

    suspend fun getUnsyncedMemories(): List<Memory> = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entities = dao.getUnsyncedMemories()
            return@withContext entities.map { MemoryEntity.toMemory(it) }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting unsynced memories", e)
            return@withContext emptyList()
        }
    }

    @Suppress("unused")
    suspend fun getMemoriesByCategory(category: String): List<Memory> = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entities = dao.getMemoriesByCategory(category)
            return@withContext entities.map { MemoryEntity.toMemory(it) }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting memories by category: $category", e)
            return@withContext emptyList()
        }
    }

    @Suppress("unused")
    suspend fun getMemoriesByImportance(minImportance: Int): List<Memory> = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entities = dao.getMemoriesByImportance(minImportance)
            return@withContext entities.map { MemoryEntity.toMemory(it) }
        } catch (e: Exception) {
            Logger.e(TAG, "Error getting memories by importance: $minImportance", e)
            return@withContext emptyList()
        }
    }
}