package com.example.eva20.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.eva20.network.models.ChatMessage

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val userId: String?,
    val isUser: Boolean,
    val timestamp: Long,
    val pending: Boolean,
    val error: Boolean,
    val synced: Boolean = false // Added to track sync status
) {
    companion object {
        // Convert from domain model to entity
        fun fromChatMessage(message: ChatMessage, synced: Boolean = false): ChatMessageEntity {
            return ChatMessageEntity(
                id = message.id,
                text = message.text,
                userId = message.userId,
                isUser = message.isUser,
                timestamp = message.timestamp,
                pending = message.pending,
                error = message.error,
                synced = synced
            )
        }
    }

    // Convert from entity to domain model
    fun toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            text = text,
            userId = userId,
            isUser = isUser,
            timestamp = timestamp,
            pending = pending,
            error = error
        )
    }
}