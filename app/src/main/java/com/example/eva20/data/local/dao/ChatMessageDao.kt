package com.example.eva20.data.local.dao

import androidx.room.*
import com.example.eva20.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE id = :id")
    suspend fun getMessageById(id: String): ChatMessageEntity?

    @Query("SELECT * FROM chat_messages WHERE synced = 0")
    suspend fun getUnsyncedMessages(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Update
    suspend fun updateMessage(message: ChatMessageEntity): Int

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: String): Int

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages(): Int

    @Query("UPDATE chat_messages SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String): Int
}