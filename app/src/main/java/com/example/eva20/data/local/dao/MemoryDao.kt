package com.example.eva20.data.local.dao

import androidx.room.*
import com.example.eva20.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemoriesFlow(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    suspend fun getAllMemories(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE synced = 0")
    suspend fun getUnsyncedMemories(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: String): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity): Int

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: String): Int

    @Suppress("unused")
    @Query("DELETE FROM memories")
    suspend fun deleteAllMessages(): Int

    @Query("UPDATE memories SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String): Int

    @Suppress("unused")
    @Query("SELECT * FROM memories WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getMemoriesByCategory(category: String): List<MemoryEntity>

    @Suppress("unused")
    @Query("SELECT * FROM memories WHERE importance >= :minImportance ORDER BY importance DESC, timestamp DESC")
    suspend fun getMemoriesByImportance(minImportance: Int): List<MemoryEntity>
}