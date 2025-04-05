package com.example.eva20.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eva20.network.websocket.WebSocketManager
import com.example.eva20.utils.Logger
import kotlinx.coroutines.launch

class CallViewModel : ViewModel() {

    private val webSocketManager = WebSocketManager()

    private val _connectionStatus = MutableLiveData<String>()
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _isMuted = MutableLiveData(false)
    val isMuted: LiveData<Boolean> = _isMuted

    fun connectWebSocket() {
        viewModelScope.launch {
            try {
                _connectionStatus.value = "Connecting..."
                webSocketManager.connect()

                webSocketManager.setOnConnectionStatusChangeListener { status ->
                    _connectionStatus.postValue(status)
                    Logger.d("CallViewModel", "WebSocket status: $status")
                }

                _connectionStatus.value = "Connected"
            } catch (e: Exception) {
                _connectionStatus.value = "Connection failed"
                Logger.e("CallViewModel", "WebSocket connection error", e)
            }
        }
    }

    fun disconnectWebSocket() {
        viewModelScope.launch {
            try {
                webSocketManager.disconnect()
                _connectionStatus.value = "Disconnected"
                Logger.d("CallViewModel", "WebSocket disconnected")
            } catch (e: Exception) {
                Logger.e("CallViewModel", "Error disconnecting WebSocket", e)
            }
        }
    }

    fun toggleMute() {
        val currentMuteState = _isMuted.value ?: false
        _isMuted.value = !currentMuteState

        viewModelScope.launch {
            try {
                webSocketManager.sendCommand(if (!currentMuteState) "mute" else "unmute")
                Logger.d("CallViewModel", "Toggled mute to ${!currentMuteState}")
            } catch (e: Exception) {
                Logger.e("CallViewModel", "Error toggling mute", e)
            }
        }
    }

    fun endCall() {
        viewModelScope.launch {
            try {
                webSocketManager.sendCommand("end_call")
                disconnectWebSocket()
            } catch (e: Exception) {
                Logger.e("CallViewModel", "Error ending call", e)
            }
        }
    }
}