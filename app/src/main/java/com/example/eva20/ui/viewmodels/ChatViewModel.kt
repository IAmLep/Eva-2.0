package com.example.eva20.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eva20.data.repository.ChatRepository
import com.example.eva20.network.models.ChatMessage
import com.example.eva20.utils.Logger
import kotlinx.coroutines.launch
import java.util.Date

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                val messages = repository.getMessages()
                _chatMessages.value = messages
                Logger.d("ChatViewModel", "Loaded ${messages.size} messages")
            } catch (e: Exception) {
                Logger.e("ChatViewModel", "Error loading messages", e)
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val message = ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    content = content,
                    timestamp = Date(),
                    isFromUser = true
                )

                // Add to local list immediately for UI responsiveness
                val currentList = _chatMessages.value?.toMutableList() ?: mutableListOf()
                currentList.add(message)
                _chatMessages.value = currentList

                // Send to backend
                repository.sendMessage(message)

                // Get response from backend
                getResponse(message)
            } catch (e: Exception) {
                Logger.e("ChatViewModel", "Error sending message", e)
            }
        }
    }

    private fun getResponse(userMessage: ChatMessage) {
        viewModelScope.launch {
            try {
                // Simulate delay for demo purposes
                kotlinx.coroutines.delay(1000)

                val responseMessage = ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    content = "This is a response to: ${userMessage.content}",
                    timestamp = Date(),
                    isFromUser = false
                )

                val currentList = _chatMessages.value?.toMutableList() ?: mutableListOf()
                currentList.add(responseMessage)
                _chatMessages.value = currentList

                // Save to repository
                repository.saveMessage(responseMessage)
            } catch (e: Exception) {
                Logger.e("ChatViewModel", "Error getting response", e)
            }
        }
    }
}