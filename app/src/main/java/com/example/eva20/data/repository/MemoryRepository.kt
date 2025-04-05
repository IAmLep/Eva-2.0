package com.example.eva20.data.repository

import com.example.eva20.data.local.DatabaseManager
import com.example.eva20.data.remote.BackendSync
import com.example.eva20.network.models.Memory
import com.example.eva20.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryRepository {
    private val tag = "MemoryRepository"

    suspend fun getLocalMemories(): List<Memory> = withContext(Dispatchers.IO) {
        try {
            Logger.d(tag, "Getting all memories from local database")
            return@withContext DatabaseManager.getAllMemories()
        } catch (e: Exception) {
            Logger.e(tag, "Error getting local memories", e)
            return@withContext emptyList()
        }
    }

    suspend fun createMemory(memory: Memory): Boolean = withContext(Dispatchers.IO) {
        try {
            // Save locally first
            DatabaseManager.addMemory(memory)
            Logger.d(tag, "Memory saved locally: ${memory.id}")
            return@withContext true
        } catch (e: Exception) {
            Logger.e(tag, "Error saving memory locally", e)
            return@withContext false
        }
    }

    suspend fun deleteMemory(memoryId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Delete locally
            DatabaseManager.deleteMemory(memoryId)
            Logger.d(tag, "Memory deleted locally: $memoryId")

            // Mark for deletion in backend
            BackendSync.markForDeletion(memoryId)

            return@withContext true
        } catch (e: Exception) {
            Logger.e(tag, "Error deleting memory", e)
            return@withContext false
        }
    }

    suspend fun syncWithCloud(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Get memories that need syncing
            val memoriesToSync = DatabaseManager.getUnsyncedMemories()
            if (memoriesToSync.isNotEmpty()) {
                Logger.d(tag, "Syncing ${memoriesToSync.size} memories with cloud")

                // Send to backend
                val syncSuccess = BackendSync.syncMemories(memoriesToSync)

                if (syncSuccess) {
                    // Mark all as synced
                    memoriesToSync.forEach { memory ->
                        DatabaseManager.markMemoryAsSynced(memory.id)
                    }
                    Logger.d(tag, "Successfully synced memories with cloud")
                } else {
                    Logger.e(tag, "Failed to sync some memories with cloud")
                }

                return@withContext syncSuccess
            }

            Logger.d(tag, "No memories to sync")
            return@withContext true
        } catch (e: Exception) {
            Logger.e(tag, "Error syncing with cloud", e)
            return@withContext false
        }
    }
}