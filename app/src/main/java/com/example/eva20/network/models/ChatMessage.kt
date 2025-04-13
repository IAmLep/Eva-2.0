package com.example.eva20.network.models

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val content: String = text,
    @SerializedName("is_user") val isFromUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val pending: Boolean = false,
    val error: Boolean = false,
    val isSynced: Boolean = false,
    // Use this field for the user_id with proper serialization name
    @SerializedName("user_id") val userId: String = "IAmLep" // Use the current user's login
)