package com.example.eva20.data.remote

import com.example.eva20.network.api.ApiService
import com.example.eva20.network.models.Memory
import com.example.eva20.utils.Logger

object BackendSync {

    // Sync memories with backend (which will handle Firebase operations)
    suspend fun syncMemories(memories: List<Memory>): Boolean {
        try {
            // Ensure authenticated first
            if (!ApiService.isAuthenticated()) {
                val authenticated = ApiService.authenticate()
                if (!authenticated) {
                    Logger.e("BackendSync", "Cannot sync: Authentication failed")
                    return false
                }
            }

            // For each memory, send to backend
            for (memory in memories) {
                try {
                    val result = ApiService.safeApiCall {
                        ApiService.apiClient.createMemory(memory)
                    }

                    if (result.isFailure) {
                        Logger.e("BackendSync", "Failed to sync memory: ${memory.id}")
                    }
                } catch (e: Exception) {
                    Logger.e("BackendSync", "Error syncing memory: ${memory.id}", e)
                }
            }

            return true
        } catch (e: Exception) {
            Logger.e("BackendSync", "Error in syncMemories", e)
            return false
        }
    }

    suspend fun markForDeletion(memoryId: String): Boolean {
        try {
            // Ensure authenticated first
            if (!ApiService.isAuthenticated()) {
                val authenticated = ApiService.authenticate()
                if (!authenticated) {
                    Logger.e("BackendSync", "Cannot delete: Authentication failed")
                    return false
                }
            }

            // Let the backend handle deletion
            val result = ApiService.safeApiCall {
                ApiService.apiClient.deleteMemory(memoryId)
            }

            return result.isSuccess
        } catch (e: Exception) {
            Logger.e("BackendSync", "Error marking memory for deletion", e)
            return false
        }
    }
}