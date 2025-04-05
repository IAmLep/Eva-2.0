package com.example.eva20.network.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Memory(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    @SerializedName("user_id")
    val userId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1, // 1-5 scale
    val category: String? = null
)