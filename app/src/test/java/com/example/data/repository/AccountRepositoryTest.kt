package com.example.data.repository

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.room.AppDatabase
import com.example.data.room.ChatMessageEntity
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.data.room.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The privacy policy promises that deleting an account removes everything it posted. These check
 * that promise against a real Room database, and that nobody else's data goes with it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class AccountRepositoryTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: AccountRepository

  @Before
  fun setUp() {
    val context: Context = ApplicationProvider.getApplicationContext()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    repository = AccountRepository(database)
  }

  @After
  fun tearDown() {
    database.close()
  }

  private fun user(id: String, mobile: String) =
    UserEntity(
      id = id,
      fullName = "User $id",
      mobile = mobile,
      email = null,
      passwordHash = "hash",
      passwordSalt = "salt",
      gender = null,
      dateOfBirth = null,
      city = "",
      state = "",
      pinCode = "",
      preferredLanguage = "en",
      createdAt = 1L,
    )

  private fun task(
    id: Int,
    owner: String,
    status: TaskStatus,
    helper: String? = null,
    photos: String = "",
  ) =
    TaskEntity(
      id = id,
      status = status,
      ownerId = owner,
      acceptedByHelperId = helper,
      categoryId = "cleaning",
      locationQuery = "Noida",
      useCurrentLocation = false,
      selectedDistance = 5,
      descriptionTitle = "Task $id",
      descriptionDetails = "",
      minBudget = "",
      maxBudget = "",
      photoUris = photos,
    )

  private suspend fun message(taskId: Int) =
    database.chatDao().insertMessage(ChatMessageEntity(taskId = taskId, senderId = "worker", text = "hi"))

  private suspend fun messageCount(taskId: Int) = database.chatDao().getMessagesForTask(taskId).first().size

  @Test
  fun `deleting an account removes the user, everything they posted and its threads`() = runBlocking {
    database.userDao().insert(user("me", "9000000001"))
    database.taskDao().insertTask(task(1, "me", TaskStatus.SUBMITTED, photos = "file:///a.jpg\nfile:///b.jpg"))
    database.taskDao().insertTask(task(2, "me", TaskStatus.DRAFT, photos = "file:///c.jpg"))
    message(1)

    val photos = repository.deleteAccount("me")

    assertNull(database.userDao().findById("me"))
    assertTrue(database.taskDao().allTasksOwnedBy("me").isEmpty())
    assertEquals(0, messageCount(1))
    assertEquals(setOf("file:///a.jpg", "file:///b.jpg", "file:///c.jpg"), photos.toSet())
  }

  @Test
  fun `an unfinished job the deleted helper had taken goes back to being open, without their chat`() =
    runBlocking {
      database.userDao().insert(user("helper", "9000000002"))
      database.taskDao().insertTask(task(3, "customer", TaskStatus.ACCEPTED, helper = "helper"))
      message(3)

      repository.deleteAccount("helper")

      val reopened = database.taskDao().observeTask(3).first()
      assertNotNull(reopened)
      assertEquals(TaskStatus.SUBMITTED, reopened!!.status)
      assertNull(reopened.acceptedByHelperId)
      assertEquals(0, messageCount(3))
    }

  @Test
  fun `other people's accounts and tasks, and finished jobs, are left alone`() = runBlocking {
    database.userDao().insert(user("me", "9000000001"))
    database.userDao().insert(user("other", "9000000003"))
    database.taskDao().insertTask(task(4, "other", TaskStatus.SUBMITTED))
    database.taskDao().insertTask(task(5, "other", TaskStatus.COMPLETED, helper = "me"))
    message(4)

    repository.deleteAccount("me")

    assertNotNull(database.userDao().findById("other"))
    assertEquals(TaskStatus.SUBMITTED, database.taskDao().observeTask(4).first()?.status)
    assertEquals(TaskStatus.COMPLETED, database.taskDao().observeTask(5).first()?.status)
    assertEquals(1, messageCount(4))
  }
}
