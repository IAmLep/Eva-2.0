package com.example.eva20.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eva20.network.websocket.WebSocketManager
import com.example.eva20.utils.Logger
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CallViewModel : ViewModel() {
    private val tag = "CallViewModel"
    private val webSocketManager = WebSocketManager()

    private val _connectionStatus = MutableLiveData<String>()
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _isMuted = MutableLiveData(false)
    val isMuted: LiveData<Boolean> = _isMuted

    // Keep track of the call start time
    @Suppress("unused")
    private val _callStartTime = MutableLiveData<String>()
    val callStartTime: LiveData<String> = _callStartTime

    // Call duration in seconds
    @Suppress("unused")
    private val _callDuration = MutableLiveData<Int>(0)
    val callDuration: LiveData<Int> = _callDuration

    fun connectWebSocket() {
        viewModelScope.launch {
            try {
                _connectionStatus.value = "Connecting..."
                webSocketManager.connect()

                webSocketManager.setOnConnectionStatusChangeListener { status ->
                    _connectionStatus.postValue(status)
                    Logger.d(tag, "WebSocket status: $status")

                    // If connected, set the call start time
                    if (status == "Connected") {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                        _callStartTime.postValue(dateFormat.format(Date()))
                    }
                }

                _connectionStatus.value = "Connected"
            } catch (e: Exception) {
                _connectionStatus.value = "Connection failed"
                Logger.e(tag, "WebSocket connection error", e)
            }
        }
    }

    fun disconnectWebSocket() {
        viewModelScope.launch {
            try {
                webSocketManager.disconnect()
                _connectionStatus.value = "Disconnected"
                Logger.d(tag, "WebSocket disconnected")
            } catch (e: Exception) {
                Logger.e(tag, "Error disconnecting WebSocket", e)
            }
        }
    }

    fun toggleMute() {
        val currentMuteState = _isMuted.value ?: false
        _isMuted.value = !currentMuteState

        viewModelScope.launch {
            try {
                webSocketManager.sendCommand(if (!currentMuteState) "mute" else "unmute")
                Logger.d(tag, "Toggled mute to ${!currentMuteState}")
            } catch (e: Exception) {
                Logger.e(tag, "Error toggling mute", e)
            }
        }
    }

    fun endCall() {
        viewModelScope.launch {
            try {
                webSocketManager.sendCommand("end_call")
                disconnectWebSocket()

                // Log call duration for analytics
                _callDuration.value?.let { duration ->
                    Logger.d(tag, "Call ended. Duration: $duration seconds")
                }
            } catch (e: Exception) {
                Logger.e(tag, "Error ending call", e)
            }
        }
    }

    // Update call duration - would be called periodically by a timer in the fragment
    fun updateCallDuration(durationInSeconds: Int) {
        _callDuration.value = durationInSeconds
    }
}