package com.example.eva20.network.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("is_user")
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val pending: Boolean = false,
    val error: Boolean = false
)