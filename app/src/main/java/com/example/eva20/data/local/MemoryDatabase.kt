package com.example.eva20.data.local

import android.content.Context
import com.example.eva20.data.local.entity.MemoryEntity
import com.example.eva20.network.models.Memory
import com.example.eva20.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-based implementation of MemoryDatabase
 */
object MemoryDatabase {
    private const val TAG = "MemoryDatabase"
    private var initialized = false
    private lateinit var db: AppDatabase

    fun initialize(context: Context) {
        if (initialized) return

        db = AppDatabase.getDatabase(context)

        initialized = true
        Logger.d(TAG, "MemoryDatabase initialized with Room")
    }

    private fun checkInitialized() {
        if (!initialized) throw IllegalStateException("MemoryDatabase not initialized")
    }

    fun getMemoriesFlow(): Flow<List<Memory>> {
        checkInitialized()
        return db.memoryDao().getAllMemoriesFlow().map { entities ->
            entities.map { it.toMemory() }
        }
    }

    suspend fun getAllMemories(): List<Memory> {
        checkInitialized()
        return db.memoryDao().getAllMemories().map { it.toMemory() }
    }

    suspend fun getUnsyncedMemories(): List<Memory> {
        checkInitialized()
        return db.memoryDao().getUnsyncedMemories().map { it.toMemory() }
    }

    suspend fun getMemory(id: String): Memory? {
        checkInitialized()
        return db.memoryDao().getMemoryById(id)?.toMemory()
    }

    suspend fun addMemory(memory: Memory) {
        checkInitialized()
        val entity = MemoryEntity.fromMemory(memory)
        db.memoryDao().insertMemory(entity)
        Logger.d(TAG, "Added memory: ${memory.id}")
    }

    suspend fun addOrUpdateMemory(memory: Memory) {
        checkInitialized()
        val entity = MemoryEntity.fromMemory(memory)
        db.memoryDao().insertMemory(entity)
        Logger.d(TAG, "Added or updated memory: ${memory.id}")
    }

    suspend fun deleteMemory(id: String) {
        checkInitialized()
        db.memoryDao().deleteMemory(id)
        Logger.d(TAG, "Deleted memory: $id")
    }

    suspend fun clearAllMemories() {
        checkInitialized()
        db.memoryDao().deleteAllMemories()
        Logger.d(TAG, "Cleared all memories")
    }

    suspend fun markAsSynced(id: String) {
        checkInitialized()
        db.memoryDao().markAsSynced(id)
        Logger.d(TAG, "Marked memory as synced: $id")
    }

    suspend fun getMemoriesByCategory(category: String): List<Memory> {
        checkInitialized()
        return db.memoryDao().getMemoriesByCategory(category).map { it.toMemory() }
    }

    suspend fun getImportantMemories(minImportance: Int = 3): List<Memory> {
        checkInitialized()
        return db.memoryDao().getMemoriesByImportance(minImportance).map { it.toMemory() }
    }
}