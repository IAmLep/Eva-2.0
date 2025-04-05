package com.example.eva20.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.eva20.network.models.Memory

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val userId: String,
    val timestamp: Long,
    val importance: Int = 1,
    val category: String? = null,
    val tags: String = "",
    val synced: Boolean = false
) {
    companion object {
        fun fromMemory(memory: Memory): MemoryEntity {
            return MemoryEntity(
                id = memory.id,
                title = memory.title,
                content = memory.text,
                userId = memory.userId ?: "IAmLep",
                timestamp = memory.timestamp,
                importance = memory.importance,
                category = memory.category,
                tags = memory.tags.joinToString(","),
                synced = memory.isSynced
            )
        }

        fun toMemory(entity: MemoryEntity): Memory {
            return Memory(
                id = entity.id,
                title = entity.title,
                text = entity.content,
                userId = entity.userId,
                timestamp = entity.timestamp,
                importance = entity.importance,
                category = entity.category,
                tags = if (entity.tags.isNotEmpty()) entity.tags.split(",") else emptyList(),
                isSynced = entity.synced
            )
        }
    }
}