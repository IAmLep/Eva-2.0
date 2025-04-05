package com.example.eva20.network.api

import com.example.eva20.network.models.ChatMessage
import com.example.eva20.network.models.Memory
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {
    // Chat endpoints
    @POST("message")
    suspend fun sendFullMessage(@Body message: ChatMessage): Response<ChatMessage>

    @POST("simple-message")
    suspend fun sendSimpleMessage(@Body message: ChatMessage): Response<ChatMessage>

    // Memory endpoints
    @GET("memory")
    suspend fun getMemories(): Response<List<Memory>>

    @POST("memory")
    suspend fun createMemory(@Body memory: Memory): Response<Memory>

    @DELETE("memory/{id}")
    suspend fun deleteMemory(@Path("id") id: String): Response<Void>

    @POST("cleanup-memories")
    suspend fun cleanupMemories(@Query("days_threshold") daysThreshold: Int): Response<Void>

    // System status
    @GET("debug")
    suspend fun getDebugInfo(): Response<Map<String, Any>>

    @GET("health")
    suspend fun getHealthStatus(): Response<Map<String, String>>
}