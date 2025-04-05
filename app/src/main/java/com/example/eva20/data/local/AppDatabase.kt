package com.example.eva20.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.eva20.data.local.dao.ChatMessageDao
import com.example.eva20.data.local.dao.MemoryDao
import com.example.eva20.data.local.entity.ChatMessageEntity
import com.example.eva20.data.local.entity.MemoryEntity
import com.example.eva20.utils.Logger

@Database(
    entities = [
        ChatMessageEntity::class,
        MemoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun memoryDao(): MemoryDao

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Logger.d(TAG, "Creating Room database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eva_database"
                )
                    .fallbackToDestructiveMigration() // For simplicity in development
                    .build()

                INSTANCE = instance
                Logger.d(TAG, "Room database instance created")
                instance
            }
        }
    }
}