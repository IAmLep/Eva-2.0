package com.example.eva20.data.remote

import com.example.eva20.network.api.ApiService
import com.example.eva20.network.models.Memory
import com.example.eva20.utils.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object BackendSync {
    private const val TAG = "BackendSync"

    // Sync memories with backend (which will handle Firebase operations)
    suspend fun syncMemories(memories: List<Memory>): Boolean {
        try {
            if (memories.isEmpty()) {
                Logger.d(TAG, "No memories to sync")
                return true
            }

            // Get current timestamp for logging
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val syncTime = dateFormat.format(Date())

            Logger.d(TAG, "Starting sync of ${memories.size} memories at $syncTime")

            // Ensure authenticated first
            if (!ApiService.isAuthenticated()) {
                val authenticated = ApiService.authenticate()
                if (!authenticated) {
                    Logger.e(TAG, "Cannot sync: Authentication failed")
                    return false
                }
            }

            // For each memory, send to backend
            var successCount = 0
            for (memory in memories) {
                try {
                    val result = ApiService.safeApiCall {
                        ApiService.apiClient.createMemory(memory)
                    }

                    if (result.isSuccess) {
                        successCount++
                    } else {
                        Logger.e(TAG, "Failed to sync memory: ${memory.id}")
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "Error syncing memory: ${memory.id}", e)
                }
            }

            Logger.d(TAG, "Sync completed. Successfully synced $successCount/${memories.size} memories")
            return successCount == memories.size // Return true only if all synced successfully
        } catch (e: Exception) {
            Logger.e(TAG, "Error in syncMemories", e)
            return false
        }
    }

    suspend fun markForDeletion(memoryId: String): Boolean {
        try {
            Logger.d(TAG, "Marking memory for deletion: $memoryId")

            // Ensure authenticated first
            if (!ApiService.isAuthenticated()) {
                val authenticated = ApiService.authenticate()
                if (!authenticated) {
                    Logger.e(TAG, "Cannot delete: Authentication failed")
                    return false
                }
            }

            // Let the backend handle deletion
            val result = ApiService.safeApiCall {
                ApiService.apiClient.deleteMemory(memoryId)
            }

            if (result.isSuccess) {
                Logger.d(TAG, "Successfully marked memory for deletion: $memoryId")
            } else {
                Logger.e(TAG, "Failed to mark memory for deletion: $memoryId")
            }

            return result.isSuccess
        } catch (e: Exception) {
            Logger.e(TAG, "Error marking memory for deletion", e)
            return false
        }
    }
}