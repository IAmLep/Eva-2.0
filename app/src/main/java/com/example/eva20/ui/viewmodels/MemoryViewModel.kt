package com.example.eva20.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eva20.data.repository.MemoryRepository
import com.example.eva20.network.models.Memory
import com.example.eva20.utils.Logger
import kotlinx.coroutines.launch

class MemoryViewModel : ViewModel() {
    private val tag = "MemoryViewModel"
    private val repository = MemoryRepository()

    private val _memories = MutableLiveData<List<Memory>>()
    val memories: LiveData<List<Memory>> = _memories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _syncStatus = MutableLiveData<String>()
    val syncStatus: LiveData<String> = _syncStatus

    init {
        loadMemories()
    }

    private fun loadMemories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loadedMemories = repository.getLocalMemories()
                _memories.value = loadedMemories
                Logger.d(tag, "Loaded ${loadedMemories.size} memories from local storage")
            } catch (e: Exception) {
                Logger.e(tag, "Error loading memories", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncWithCloud() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _syncStatus.value = "Syncing..."
                val success = repository.syncWithCloud()
                if (success) {
                    loadMemories() // Reload after sync
                    _syncStatus.value = "Sync completed"
                    Logger.d(tag, "Sync with cloud completed")
                } else {
                    _syncStatus.value = "Sync partially failed"
                    Logger.w(tag, "Sync with cloud partially failed")
                    loadMemories() // Still reload to get latest data
                }
            } catch (e: Exception) {
                _syncStatus.value = "Sync failed"
                Logger.e(tag, "Error syncing with cloud", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMemory(memoryId: String) {
        viewModelScope.launch {
            try {
                val success = repository.deleteMemory(memoryId)
                if (success) {
                    Logger.d(tag, "Memory deleted: $memoryId")
                    // Update UI immediately
                    _memories.value = _memories.value?.filter { it.id != memoryId }
                } else {
                    Logger.e(tag, "Failed to delete memory: $memoryId")
                }
            } catch (e: Exception) {
                Logger.e(tag, "Error deleting memory", e)
            }
        }
    }

    fun createMemory(title: String, text: String, importance: Int = 1, category: String? = null) {
        viewModelScope.launch {
            try {
                val memory = Memory(
                    id = java.util.UUID.randomUUID().toString(),
                    title = title,
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    importance = importance,
                    category = category,
                    userId = "IAmLep", // Using the current user's login
                    tags = emptyList()
                )

                val success = repository.createMemory(memory)
                if (success) {
                    Logger.d(tag, "Memory created: ${memory.id}")
                    loadMemories() // Reload to include the new memory
                } else {
                    Logger.e(tag, "Failed to create memory")
                }
            } catch (e: Exception) {
                Logger.e(tag, "Error creating memory", e)
            }
        }
    }
}