package com.example.eva20.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.eva20.network.models.Memory

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val userId: String?,
    val timestamp: Long,
    val importance: Int,
    val category: String?,
    val synced: Boolean = false
) {
    companion object {
        fun fromMemory(memory: Memory, synced: Boolean = false): MemoryEntity {
            return MemoryEntity(
                id = memory.id,
                text = memory.text,
                userId = memory.userId,
                timestamp = memory.timestamp,
                importance = memory.importance,
                category = memory.category,
                synced = synced
            )
        }
    }

    fun toMemory(): Memory {
        return Memory(
            id = id,
            text = text,
            userId = userId,
            timestamp = timestamp,
            importance = importance,
            category = category
        )
    }
}