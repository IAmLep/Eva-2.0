package com.example.eva20.network.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val content: String = text, // Added for backward compatibility with UI
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("is_user")
    val isUser: Boolean = false,
    val isFromUser: Boolean = isUser, // Added for backward compatibility with UI
    val timestamp: Long = System.currentTimeMillis(),
    val pending: Boolean = false,
    val error: Boolean = false,
    val isSynced: Boolean = false // Added for repository usage
) {
    // Helper function to get Date object for UI formatting
    fun getDate(): Date = Date(timestamp)
}