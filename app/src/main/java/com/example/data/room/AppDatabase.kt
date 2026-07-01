package com.example.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class, ChatMessageEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun chatDao(): ChatDao
}
