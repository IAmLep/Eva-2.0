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
    private val tag = "DatabaseManager"
    private var database: AppDatabase? = null

    // Current time and user information
    private val currentTime: String
        get() {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            return dateFormat.format(Date())
        }

    private val currentUser: String = "IAmLep"

    fun initialize(context: Context) {
        if (database == null) {
            database = AppDatabase.getDatabase(context)
            Logger.d(tag, "Database initialized at $currentTime by $currentUser")
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
            Logger.e(tag, "Error getting all chat messages", e)
            return@withContext emptyList()
        }
    }

    fun getAllMessagesFlow(): Flow<List<ChatMessage>> {
        val dao = database?.chatMessageDao()
            ?: throw IllegalStateException("Database not initialized")

        return dao.getAllMessagesFlow()
            .map { entities -> entities.map { ChatMessageEntity.toChatMessage(it) } }
    }

    suspend fun getMessageById(messageId: String): ChatMessage? = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = dao.getMessageById(messageId)
            return@withContext entity?.let { ChatMessageEntity.toChatMessage(it) }
        } catch (e: Exception) {
            Logger.e(tag, "Error getting message by ID: $messageId", e)
            return@withContext null
        }
    }

    suspend fun addMessage(message: ChatMessage): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = ChatMessageEntity.fromChatMessage(message)
            val insertResult = dao.insertMessage(entity)
            Logger.d(tag, "Chat message added with ID: ${message.id} by $currentUser at $currentTime")
            return@withContext insertResult > 0
        } catch (e: Exception) {
            Logger.e(tag, "Error adding chat message", e)
            return@withContext false
        }
    }

    suspend fun updateMessage(message: ChatMessage): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = ChatMessageEntity.fromChatMessage(message)
            val updateResult = dao.updateMessage(entity)
            Logger.d(tag, "Chat message updated with ID: ${message.id} by $currentUser at $currentTime")
            return@withContext updateResult > 0
        } catch (e: Exception) {
            Logger.e(tag, "Error updating chat message", e)
            return@withContext false
        }
    }

    suspend fun deleteMessage(messageId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val deleteResult = dao.deleteMessage(messageId)
            Logger.d(tag, "Chat message deleted with ID: $messageId by $currentUser at $currentTime")
            return@withContext deleteResult > 0
        } catch (e: Exception) {
            Logger.e(tag, "Error deleting chat message", e)
            return@withContext false
        }
    }

    suspend fun markMessageAsSynced(messageId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val updateResult = dao.markAsSynced(messageId)
            Logger.d(tag, "Chat message marked as synced: $messageId by $currentUser at $currentTime")
            return@withContext updateResult > 0
        } catch (e: Exception) {
            Logger.e(tag, "Error marking chat message as synced", e)
            return@withContext false
        }
    }

    suspend fun getUnsyncedMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val dao = database?.chatMessageDao()
                ?: throw IllegalStateException("Database not initialized")

            val entities = dao.getUnsyncedMessages()
            return@withContext entities.map { ChatMessageEntity.toChatMessage(it) }
        } catch (e: Exception) {
            Logger.e(tag, "Error getting unsynced chat messages", e)
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
            Logger.e(tag, "Error getting all memories", e)
            return@withContext emptyList()
        }
    }

    fun getAllMemoriesFlow(): Flow<List<Memory>> {
        val dao = database?.memoryDao()
            ?: throw IllegalStateException("Database not initialized")

        return dao.getAllMemoriesFlow()
            .map { entities -> entities.map { MemoryEntity.toMemory(it) } }
    }

    suspend fun getMemoryById(memoryId: String): Memory? = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = dao.getMemoryById(memoryId)
            return@withContext entity?.let { MemoryEntity.toMemory(it) }
        } catch (e: Exception) {
            Logger.e(tag, "Error getting memory by ID: $memoryId", e)
            return@withContext null
        }
    }

    suspend fun addMemory(memory: Memory): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = MemoryEntity.fromMemory(memory)
            val insertResult = dao.insertMemory(entity)
            Logger.d(tag, "Memory added with ID: ${memory.id} by $currentUser at $currentTime")
            return@withContext insertResult > 0
        } catch (e: Exception) {
            Logger.e(tag, "Error adding memory", e)
            return@withContext false
        }
    }

    suspend fun updateMemory(memory: Memory): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entity = MemoryEntity.fromMemory(memory)
            val updateResult = dao.updateMemory(entity)
            Logger.d(tag, "Memory updated with ID: ${memory.id} by $currentUser at $currentTime")
            return@withContext updateResult > 0
        } catch (e: Exception) {
            Logger.e(tag, "Error updating memory", e)
            return@withContext false
        }
    }

    suspend fun deleteMemory(memoryId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val deleteResult = dao.deleteMemory(memoryId)
            Logger.d(tag, "Memory deleted with ID: $memoryId by $currentUser at $currentTime")
            return@withContext deleteResult > 0
        } catch (e: Exception) {
            Logger.e(tag, "Error deleting memory", e)
            return@withContext false
        }
    }

    suspend fun markMemoryAsSynced(memoryId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val updateResult = dao.markAsSynced(memoryId)
            Logger.d(tag, "Memory marked as synced: $memoryId by $currentUser at $currentTime")
            return@withContext updateResult > 0
        } catch (e: Exception) {
            Logger.e(tag, "Error marking memory as synced", e)
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
            Logger.e(tag, "Error getting unsynced memories", e)
            return@withContext emptyList()
        }
    }

    suspend fun getMemoriesByCategory(category: String): List<Memory> = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entities = dao.getMemoriesByCategory(category)
            return@withContext entities.map { MemoryEntity.toMemory(it) }
        } catch (e: Exception) {
            Logger.e(tag, "Error getting memories by category: $category", e)
            return@withContext emptyList()
        }
    }

    suspend fun getMemoriesByImportance(minImportance: Int): List<Memory> = withContext(Dispatchers.IO) {
        try {
            val dao = database?.memoryDao()
                ?: throw IllegalStateException("Database not initialized")

            val entities = dao.getMemoriesByImportance(minImportance)
            return@withContext entities.map { MemoryEntity.toMemory(it) }
        } catch (e: Exception) {
            Logger.e(tag, "Error getting memories by importance: $minImportance", e)
            return@withContext emptyList()
        }
    }
}