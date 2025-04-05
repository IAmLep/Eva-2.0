package com.example.eva20.data.repository

import com.example.eva20.data.local.MemoryDatabase
import com.example.eva20.data.remote.BackendSync
import com.example.eva20.network.api.ApiService
import com.example.eva20.network.models.Memory
import com.example.eva20.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryRepository {

    private val memoryDatabase = MemoryDatabase

    suspend fun getLocalMemories(): List<Memory> = withContext(Dispatchers.IO) {
        try {
            memoryDatabase.getAllMemories()
        } catch (e: Exception) {
            Logger.e("MemoryRepository", "Error getting local memories", e)
            emptyList()
        }
    }

    suspend fun syncWithCloud() = withContext(Dispatchers.IO) {
        try {
            // First ensure we're authenticated
            if (!ApiService.isAuthenticated()) {
                val authenticated = ApiService.authenticate()
                if (!authenticated) {
                    throw Exception("Authentication failed")
                }
            }

            // Get unsynced memories and send to backend
            val unsyncedMemories = memoryDatabase.getUnsyncedMemories()
            if (unsyncedMemories.isNotEmpty()) {
                BackendSync.syncMemories(unsyncedMemories)

                // Mark synced
                for (memory in unsyncedMemories) {
                    memoryDatabase.markAsSynced(memory.id)
                }
            }

            // Get latest data from backend
            val result = ApiService.safeApiCall {
                ApiService.apiClient.getMemories()
            }

            if (result.isSuccess) {
                val remoteMemories = result.getOrNull() ?: emptyList()
                memoryDatabase.updateMemoriesFromRemote(remoteMemories)
            }

        } catch (e: Exception) {
            Logger.e("MemoryRepository", "Error syncing with cloud", e)
            throw e
        }
    }

    suspend fun deleteMemory(memoryId: String) = withContext(Dispatchers.IO) {
        try {
            // Delete locally first
            memoryDatabase.deleteMemory(memoryId)

            // Try to delete from backend
            BackendSync.markForDeletion(memoryId)

        } catch (e: Exception) {
            Logger.e("MemoryRepository", "Error deleting memory", e)
            false
        }
    }
}