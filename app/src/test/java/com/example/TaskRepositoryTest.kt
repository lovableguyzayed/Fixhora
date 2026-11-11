package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.repository.TaskRepository
import com.example.data.room.AppDatabase
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: TaskRepository

    private fun task(status: String, title: String) = TaskEntity(
        status = status, categoryId = "repairs", locationQuery = "Civil Lines, Kanpur",
        useCurrentLocation = false, selectedDistance = 5, descriptionTitle = title,
        descriptionDetails = "", minBudget = "500", maxBudget = "1500", photoUris = ""
    )

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries().build()
        repo = TaskRepository(db.taskDao())
    }

    @After fun tearDown() = db.close()

    @Test fun postingATaskLeavesNoDraftBehind() = runBlocking {
        repo.saveDraft(task("draft", "Fix tap"))
        repo.saveDraft(task("draft", "Fix kitchen tap")) // editing keeps a single draft
        repo.submitTask(task("draft", "Fix kitchen tap"))

        assertNull(repo.getDraftTask())
        val posted = repo.allTasks.first()
        assertEquals(1, posted.size)
        assertEquals("submitted", posted[0].status)
        assertEquals("Fix kitchen tap", posted[0].descriptionTitle)
    }

    @Test fun demoTasksAreSeededOnlyOnce() = runBlocking {
        repo.seedDemoTasksIfEmpty()
        repo.seedDemoTasksIfEmpty()
        assertEquals(6, repo.allTasks.first().size)
    }

    @Test fun demoTasksAreNotAddedWhenRealTasksExist() = runBlocking {
        repo.submitTask(task("submitted", "Real task"))
        repo.seedDemoTasksIfEmpty()
        assertEquals(1, repo.allTasks.first().size)
    }
}
