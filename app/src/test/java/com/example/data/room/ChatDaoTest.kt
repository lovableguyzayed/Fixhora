package com.example.data.room

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Message storage, which both halves of every conversation depend on.
 *
 * The ordering matters beyond tidiness: both chat screens render with `reverseLayout = true` on the
 * assumption that the DAO returns newest first. Flip the `ORDER BY` and every conversation reads
 * backwards.
 */
@RunWith(RobolectricTestRunner::class)
// A plain Application, not FixhoraApplication: the composition root opens the production database
// and seeds demo data in onCreate, which has no business running inside a database test.
@Config(sdk = [34], application = Application::class)
class ChatDaoTest {

  private lateinit var database: AppDatabase
  private lateinit var dao: ChatDao
  private lateinit var repository: ChatRepository

  @Before
  fun setUp() {
    val context: Context = ApplicationProvider.getApplicationContext()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    dao = database.chatDao()
    repository = ChatRepository(dao)
  }

  @After
  fun tearDown() {
    database.close()
  }

  private fun message(id: Int, taskId: Int, sender: String, text: String, at: Long) =
    ChatMessageEntity(id = id, taskId = taskId, senderId = sender, text = text, timestamp = at)

  @Test
  fun `messages come back newest first`() = runBlocking {
    dao.insertMessage(message(1, 1, ChatRepository.SENDER_CUSTOMER, "First", 1_000L))
    dao.insertMessage(message(2, 1, ChatRepository.SENDER_WORKER, "Second", 2_000L))
    dao.insertMessage(message(3, 1, ChatRepository.SENDER_CUSTOMER, "Third", 3_000L))

    assertEquals(
      listOf("Third", "Second", "First"),
      dao.getMessagesForTask(1).first().map { it.text },
    )
  }

  /** One customer's thread must never leak into another task's conversation. */
  @Test
  fun `messages are scoped to their task`() = runBlocking {
    dao.insertMessage(message(1, 1, ChatRepository.SENDER_CUSTOMER, "About task 1", 1_000L))
    dao.insertMessage(message(2, 2, ChatRepository.SENDER_CUSTOMER, "About task 2", 1_000L))

    assertEquals(listOf("About task 1"), dao.getMessagesForTask(1).first().map { it.text })
    assertEquals(listOf("About task 2"), dao.getMessagesForTask(2).first().map { it.text })
  }

  @Test
  fun `a task with no conversation yet returns nothing, not everything`() = runBlocking {
    dao.insertMessage(message(1, 1, ChatRepository.SENDER_CUSTOMER, "Hello", 1_000L))

    assertTrue(dao.getMessagesForTask(99).first().isEmpty())
  }

  /**
   * Both sides write through the repository. The sender has to survive the round trip, or every
   * bubble renders on the wrong side of the screen.
   */
  @Test
  fun `both senders round-trip through the repository`() = runBlocking {
    repository.sendMessage(1, ChatRepository.SENDER_CUSTOMER, "Can you come today?")
    repository.sendMessage(1, ChatRepository.SENDER_WORKER, "Yes, after 4pm")

    val messages = dao.getMessagesForTask(1).first()

    assertEquals(2, messages.size)
    assertEquals(setOf(ChatRepository.SENDER_CUSTOMER, ChatRepository.SENDER_WORKER),
      messages.map { it.senderId }.toSet())
    assertTrue(messages.all { it.text.isNotBlank() })
  }

  @Test
  fun `a sent message carries a real timestamp`() = runBlocking {
    val before = System.currentTimeMillis()

    repository.sendMessage(1, ChatRepository.SENDER_CUSTOMER, "Hello")

    assertTrue(dao.getMessagesForTask(1).first().first().timestamp >= before)
  }
}
