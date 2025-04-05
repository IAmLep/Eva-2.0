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
                Logger.d("MemoryViewModel", "Loaded ${loadedMemories.size} memories from local storage")
            } catch (e: Exception) {
                Logger.e("MemoryViewModel", "Error loading memories", e)
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
                repository.syncWithCloud()
                loadMemories() // Reload after sync
                _syncStatus.value = "Sync completed"
                Logger.d("MemoryViewModel", "Sync with cloud completed")
            } catch (e: Exception) {
                _syncStatus.value = "Sync failed"
                Logger.e("MemoryViewModel", "Error syncing with cloud", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMemory(memoryId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMemory(memoryId)
                Logger.d("MemoryViewModel", "Memory deleted: $memoryId")

                // Update UI immediately
                _memories.value = _memories.value?.filter { it.id != memoryId }

                // Sync deletion with cloud
                syncWithCloud()
            } catch (e: Exception) {
                Logger.e("MemoryViewModel", "Error deleting memory", e)
            }
        }
    }
}