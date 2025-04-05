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
    val synced: Boolean = false,
    val pending: Boolean = false,
    val error: Boolean = false
) {
    companion object {
        fun fromChatMessage(message: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = message.id,
                text = message.text,
                userId = message.userId,
                isUser = message.isUser,
                timestamp = message.timestamp,
                synced = message.isSynced,
                pending = message.pending,
                error = message.error
            )
        }

        fun toChatMessage(entity: ChatMessageEntity): ChatMessage {
            return ChatMessage(
                id = entity.id,
                text = entity.text,
                userId = entity.userId,
                isUser = entity.isUser,
                timestamp = entity.timestamp,
                pending = entity.pending,
                error = entity.error,
                isSynced = entity.synced
            )
        }
    }
}