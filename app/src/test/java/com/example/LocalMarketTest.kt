package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.market.LocalMarketRepository
import com.example.data.market.Offer
import com.example.data.market.Task
import com.example.data.repository.ChatRepository
import com.example.data.repository.TaskRepository
import com.example.data.room.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** The whole customer ↔ helper flow through the on-phone version of the marketplace. */
@RunWith(AndroidJUnit4::class)
class LocalMarketTest {
    private lateinit var db: AppDatabase
    private lateinit var market: LocalMarketRepository

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).allowMainThreadQueries().build()
        market = LocalMarketRepository(TaskRepository(db.taskDao(), db.offerDao()), ChatRepository(db.chatDao()))
    }
    @After fun tearDown() = db.close()

    @Test fun postOfferChooseChatDone() = runBlocking {
        market.postTask(Task("", "cust", "Ramesh", "submitted", "repairs", "Civil Lines, Kanpur", "Fix tap", "", "300", "800")).getOrThrow()
        val task = market.openJobs().first().single()
        assertEquals("Fix tap", task.title)

        market.sendOffer(Offer("", task.id, "h1", "Suresh", 650)).getOrThrow()
        market.sendOffer(Offer("", task.id, "h2", "Anil", 500)).getOrThrow()
        val offers = market.offersOnTasksOf("cust").first()
        assertEquals(2, offers.size)

        market.chooseOffer(task, offers.first { it.helperId == "h1" }, offers).getOrThrow()
        assertEquals("accepted", market.tasksOwnedBy("cust").first().single().status)
        assertEquals(0, market.openJobs().first().size)
        val suresh = market.offersSentBy("h1").first().single()
        assertEquals("accepted", suresh.first.status)
        assertEquals("declined", market.offersSentBy("h2").first().single().first.status)

        market.sendMessage(task.id, "customer", "Namaste").getOrThrow()
        market.sendMessage(task.id, "worker", "Aa raha hoon").getOrThrow()
        assertEquals(listOf("Aa raha hoon", "Namaste"), market.messages(task.id).first().map { it.text })

        market.setStatus(market.tasksOwnedBy("cust").first().single(), "completed").getOrThrow()
        assertEquals("completed", market.offersSentBy("h1").first().single().second?.status)
    }
}
