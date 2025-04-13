package com.example.eva20.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eva20.data.repository.ChatRepository
import com.example.eva20.network.models.ChatMessage
import com.example.eva20.utils.Logger
import kotlinx.coroutines.launch

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
                    text = content,
                    timestamp = System.currentTimeMillis(),
                    isFromUser = true,
                    userId = "IAmLep"
                )

                // Add user message to local list immediately for UI responsiveness
                val currentList = _chatMessages.value?.toMutableList() ?: mutableListOf()
                currentList.add(message)
                _chatMessages.value = currentList

                // Send to backend and get response
                val botResponse = repository.sendMessage(message)

                // If we got a response from the API, add it to the chat
                if (botResponse != null) {
                    Logger.d("ChatViewModel", "Received bot response: ${botResponse.text}")

                    // Add bot response to UI
                    val updatedList = _chatMessages.value?.toMutableList() ?: mutableListOf()
                    updatedList.add(botResponse)
                    _chatMessages.value = updatedList
                } else {
                    Logger.e("ChatViewModel", "No response received from API")
                    // Optionally show an error message or fallback response
                }
            } catch (e: Exception) {
                Logger.e("ChatViewModel", "Error sending message", e)
            }
        }
    }
}