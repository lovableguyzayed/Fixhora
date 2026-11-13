package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.auth.AuthRepository
import com.example.data.auth.FirebaseAuthRepository
import com.example.data.auth.TestModeAuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.TaskRepository
import com.example.data.room.AppDatabase
import com.example.util.PhotoStore
import com.google.firebase.FirebaseApp

class FixhoraApplication : Application() {
    lateinit var database: AppDatabase
    lateinit var taskRepository: TaskRepository
    lateinit var chatRepository: ChatRepository
    lateinit var authRepository: AuthRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "fixhora-database")
            .addMigrations(AppDatabase.MIGRATION_2_3)
            // Only for very old test installs (version 1); real users' data is migrated above.
            .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1)
            .build()

        taskRepository = TaskRepository(database.taskDao(), database.offerDao())
        chatRepository = ChatRepository(database.chatDao())
        // Real Firebase login when google-services.json is in the project, otherwise test mode.
        authRepository = if (FirebaseApp.getApps(this).isNotEmpty()) FirebaseAuthRepository(this)
        else TestModeAuthRepository(this)
    }

    /** Logout / delete account: remove everything this person left on the phone. */
    suspend fun wipeLocalData() {
        taskRepository.deleteAllLocalData()
        chatRepository.deleteAll()
        PhotoStore.deleteAll(this)
    }
}
