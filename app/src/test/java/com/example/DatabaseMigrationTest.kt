package com.example

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.repository.TaskRepository
import com.example.data.room.AppDatabase
import com.example.data.room.OfferEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** Phones already have version 2 of the database; the update must keep their tasks. */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    @Test fun version2DataSurvivesUpgradeTo3() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val name = "migration-test.db"
        ctx.deleteDatabase(name)
        // Exactly the tables Room created for version 2 (copied from the generated code).
        SQLiteDatabase.openOrCreateDatabase(ctx.getDatabasePath(name).apply { parentFile?.mkdirs() }, null).use { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `status` TEXT NOT NULL, `categoryId` TEXT, `locationQuery` TEXT NOT NULL, `useCurrentLocation` INTEGER NOT NULL, `selectedDistance` INTEGER NOT NULL, `descriptionTitle` TEXT NOT NULL, `descriptionDetails` TEXT NOT NULL, `minBudget` TEXT NOT NULL, `maxBudget` TEXT NOT NULL, `photoUris` TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `senderId` TEXT NOT NULL, `text` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `status` TEXT NOT NULL)")
            db.execSQL("INSERT INTO tasks (status, categoryId, locationQuery, useCurrentLocation, selectedDistance, descriptionTitle, descriptionDetails, minBudget, maxBudget, photoUris) VALUES ('submitted','repairs','Civil Lines, Kanpur',0,5,'Fix tap','','500','1500','')")
            db.execSQL("INSERT INTO chat_messages (taskId, senderId, text, timestamp, status) VALUES (1,'worker','Namaste',1,'sent')")
            db.version = 2
        }

        val room = Room.databaseBuilder(ctx, AppDatabase::class.java, name)
            .addMigrations(AppDatabase.MIGRATION_2_3).allowMainThreadQueries().build()
        val repo = TaskRepository(room.taskDao(), room.offerDao())
        val tasks = repo.allTasks.first()
        assertEquals(1, tasks.size)
        assertEquals("Fix tap", tasks[0].descriptionTitle)
        assertNull(tasks[0].ownerId)
        assertEquals(0L, tasks[0].createdAt)
        assertEquals(1, room.chatDao().getMessagesForTask(1).first().size)
        // New table works after the upgrade.
        repo.sendOffer(OfferEntity(taskId = 1, helperId = "h1", helperName = "Suresh", amount = 650))
        assertEquals(1, repo.offersForTask(1).first().size)
        room.close()
    }
}
