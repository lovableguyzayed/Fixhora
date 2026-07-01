package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.repository.TaskRepository
import com.example.data.room.AppDatabase

import com.example.data.repository.ChatRepository

class FixhoraApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var taskRepository: TaskRepository
    lateinit var chatRepository: ChatRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fixhora-database"
        ).fallbackToDestructiveMigration().build()
        
        taskRepository = TaskRepository(database.taskDao())
        chatRepository = ChatRepository(database.chatDao())
    }
}
