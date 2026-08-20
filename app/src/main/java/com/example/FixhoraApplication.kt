package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.media.TaskPhotoStore
import com.example.data.repository.ChatRepository
import com.example.data.repository.TaskRepository
import com.example.data.repository.UserRepository
import com.example.data.room.AppDatabase
import com.example.data.seed.DemoDataSeeder
import com.example.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Composition root. Holds the single database instance and the repositories built on it; screens
 * reach these through `LocalContext.current.applicationContext as FixhoraApplication`.
 */
class FixhoraApplication : Application() {

  lateinit var database: AppDatabase
    private set

  lateinit var taskRepository: TaskRepository
    private set

  lateinit var chatRepository: ChatRepository
    private set

  lateinit var userRepository: UserRepository
    private set

  lateinit var sessionManager: SessionManager
    private set

  lateinit var taskPhotoStore: TaskPhotoStore
    private set

  private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onCreate() {
    super.onCreate()

    database =
      Room.databaseBuilder(applicationContext, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
        .addMigrations(AppDatabase.MIGRATION_2_3)
        .apply {
          if (BuildConfig.DEBUG) {
            // Schema churn is expected while developing, and a wipe is preferable to a crash loop.
            fallbackToDestructiveMigration(dropAllTables = true)
          } else {
            // Release must never silently delete a user's tasks, so only the pre-release v1
            // schema — which no shipped build ever wrote — is allowed to be dropped. Any other
            // missing migration surfaces as an error instead of as data loss.
            fallbackToDestructiveMigrationFrom(dropAllTables = true, 1)
          }
        }
        .build()

    taskRepository = TaskRepository(database)
    chatRepository = ChatRepository(database.chatDao())
    userRepository = UserRepository(database.userDao())
    sessionManager = SessionManager(this)
    taskPhotoStore = TaskPhotoStore(this)

    if (BuildConfig.DEBUG) {
      // Seeded once, from here only. Previously both TaskViewModel and HelperViewModel seeded on
      // init, which could race into a doubled data set, and it ran in release builds too.
      applicationScope.launch { DemoDataSeeder(database.taskDao()).seedIfEmpty() }
    }
  }
}
