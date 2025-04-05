package com.example.eva20.network.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Memory(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",  // Added as it's used in MemoryAdapter
    val text: String,
    val content: String = text, // For compatibility with adapter
    @SerializedName("user_id")
    val userId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1, // 1-5 scale
    val category: String? = null,
    val tags: List<String> = emptyList(), // Added as it's used in MemoryAdapter
    val isSynced: Boolean = false // Added as it's used in MemoryAdapter
) {
    // Helper method to get Date from timestamp for UI
    fun getDate(): Date = Date(timestamp)
}