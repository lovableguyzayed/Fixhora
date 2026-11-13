package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.repository.TaskRepository
import com.example.data.room.AppDatabase
import com.example.data.room.OfferEntity
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfferFlowTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: TaskRepository

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).allowMainThreadQueries().build()
        repo = TaskRepository(db.taskDao(), db.offerDao())
    }
    @After fun tearDown() = db.close()

    private suspend fun postTask(owner: String): Int {
        repo.submitTask(TaskEntity(status = "draft", categoryId = "repairs", locationQuery = "Gomti Nagar, Lucknow",
            useCurrentLocation = false, selectedDistance = 5, descriptionTitle = "Leaking tap", descriptionDetails = "",
            minBudget = "300", maxBudget = "800", photoUris = "", ownerId = owner))
        return repo.allTasks.first().first().id
    }

    @Test fun choosingAnOfferDeclinesTheOthersAndAcceptsTheTask() = runBlocking {
        val id = postTask("cust1")
        assertTrue(repo.sendOffer(OfferEntity(taskId = id, helperId = "h1", helperName = "Suresh", amount = 650)))
        assertTrue(repo.sendOffer(OfferEntity(taskId = id, helperId = "h2", helperName = "Anil", amount = 500)))
        assertFalse("one offer per helper", repo.sendOffer(OfferEntity(taskId = id, helperId = "h1", helperName = "Suresh", amount = 400)))

        val suresh = repo.offersForTask(id).first().first { it.helperId == "h1" }
        repo.acceptOffer(id, suresh.id)

        val offers = repo.offersForTask(id).first().associateBy { it.helperId }
        assertEquals("accepted", offers.getValue("h1").status)
        assertEquals("declined", offers.getValue("h2").status)
        assertEquals("accepted", repo.allTasks.first().first().status)
    }

    @Test fun myTasksShowsOnlyMyTasksAndGuestTasksAreClaimedOnLogin() = runBlocking {
        postTask("guest"); postTask("someone-else")
        assertEquals(1, repo.tasksOwnedBy("guest").first().size)
        repo.claimGuestTasks("ramesh")
        assertEquals(1, repo.tasksOwnedBy("ramesh").first().size)
        assertEquals(0, repo.tasksOwnedBy("guest").first().size)
    }

    @Test fun wipeRemovesEverything() = runBlocking {
        val id = postTask("cust1")
        repo.sendOffer(OfferEntity(taskId = id, helperId = "h1", helperName = "Suresh", amount = 650))
        repo.deleteAllLocalData()
        assertTrue(repo.allTasks.first().isEmpty())
        assertTrue(repo.allOffers.first().isEmpty())
    }
}
