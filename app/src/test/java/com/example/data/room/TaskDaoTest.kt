package com.example.data.room

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
 * First coverage for the task queries.
 *
 * The two added in Batch 8 — `getTasksForOwner` and `observeTask` — drive the customer's task list
 * and its detail screen, and until now nothing had ever run them. A wrong `WHERE` here shows one
 * customer another customer's tasks, or leaks an unfinished draft into a list of things they
 * actually asked for.
 */
@RunWith(RobolectricTestRunner::class)
// A plain Application, not FixhoraApplication: the composition root opens the production database
// and seeds demo data in onCreate, which has no business running inside a database test.
@Config(sdk = [34], application = Application::class)
class TaskDaoTest {

  private lateinit var database: AppDatabase
  private lateinit var dao: TaskDao

  @Before
  fun setUp() {
    val context: Context = ApplicationProvider.getApplicationContext()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    dao = database.taskDao()
  }

  @After
  fun tearDown() {
    database.close()
  }

  private fun task(
    id: Int,
    ownerId: String,
    status: TaskStatus,
    title: String = "Task $id",
    createdAt: Long = id * 1_000L,
  ) =
    TaskEntity(
      id = id,
      status = status,
      ownerId = ownerId,
      categoryId = "repairs",
      locationQuery = "Noida",
      useCurrentLocation = false,
      selectedDistance = 5,
      descriptionTitle = title,
      descriptionDetails = "",
      minBudget = "",
      maxBudget = "",
      photoUris = "",
      createdAt = createdAt,
    )

  private suspend fun seed(vararg tasks: TaskEntity) = tasks.forEach { dao.insertTask(it) }

  @Test
  fun `tasks for an owner exclude other owners`() = runBlocking {
    seed(
      task(1, "anita", TaskStatus.SUBMITTED),
      task(2, "bharat", TaskStatus.SUBMITTED),
      task(3, "anita", TaskStatus.COMPLETED),
    )

    val anitas = dao.getTasksForOwner("anita").first()

    assertEquals(listOf(3, 1), anitas.map { it.id })
  }

  /** A half-filled wizard is not something the customer asked for, so it stays out of the list. */
  @Test
  fun `tasks for an owner exclude their own draft`() = runBlocking {
    seed(task(1, "anita", TaskStatus.DRAFT), task(2, "anita", TaskStatus.SUBMITTED))

    val anitas = dao.getTasksForOwner("anita").first()

    assertEquals(listOf(2), anitas.map { it.id })
  }

  @Test
  fun `tasks for an owner are newest first`() = runBlocking {
    seed(
      task(1, "anita", TaskStatus.SUBMITTED, createdAt = 1_000L),
      task(2, "anita", TaskStatus.SUBMITTED, createdAt = 3_000L),
      task(3, "anita", TaskStatus.SUBMITTED, createdAt = 2_000L),
    )

    assertEquals(listOf(2, 3, 1), dao.getTasksForOwner("anita").first().map { it.id })
  }

  @Test
  fun `an owner with nothing posted gets an empty list, not everyone's tasks`() = runBlocking {
    seed(task(1, "anita", TaskStatus.SUBMITTED))

    assertTrue(dao.getTasksForOwner("chetan").first().isEmpty())
  }

  /** The detail screen closes itself on null, so the null has to actually arrive. */
  @Test
  fun `observing a task emits it, then null once it is deleted`() = runBlocking {
    seed(task(1, "anita", TaskStatus.SUBMITTED, title = "Fix the fan"))

    assertEquals("Fix the fan", dao.observeTask(1).first()?.descriptionTitle)

    dao.deleteTaskById(1)

    assertNull(dao.observeTask(1).first())
  }

  @Test
  fun `observing a task that never existed emits null`() = runBlocking {
    assertNull(dao.observeTask(404).first())
  }

  @Test
  fun `the helper feed shows only submitted tasks`() = runBlocking {
    seed(
      task(1, "anita", TaskStatus.DRAFT),
      task(2, "anita", TaskStatus.SUBMITTED),
      task(3, "anita", TaskStatus.ACCEPTED),
      task(4, "anita", TaskStatus.COMPLETED),
    )

    assertEquals(listOf(2), dao.getTasksByStatus(TaskStatus.SUBMITTED).first().map { it.id })
  }

  @Test
  fun `every posted task is visible, but never a draft`() = runBlocking {
    seed(
      task(1, "anita", TaskStatus.DRAFT),
      task(2, "anita", TaskStatus.SUBMITTED),
      task(3, "bharat", TaskStatus.CANCELLED),
    )

    val posted = dao.getAllPostedTasks().first()

    assertEquals(setOf(2, 3), posted.map { it.id }.toSet())
    assertEquals(2, dao.countPostedTasks())
  }

  /** One draft per owner is the invariant the whole autosave path depends on. */
  @Test
  fun `each owner has their own draft`() = runBlocking {
    seed(task(1, "anita", TaskStatus.DRAFT), task(2, "bharat", TaskStatus.DRAFT))

    assertEquals(1, dao.getDraftTask("anita")?.id)
    assertEquals(2, dao.getDraftTask("bharat")?.id)
    assertNull(dao.getDraftTask("chetan"))
  }

  /** `TaskStatus` is stored through a converter; a broken one would surface here. */
  @Test
  fun `every status round-trips through the database`() = runBlocking {
    TaskStatus.entries.forEachIndexed { index, status ->
      dao.insertTask(task(index + 1, "anita", status))
    }

    TaskStatus.entries.forEachIndexed { index, status ->
      val stored = dao.observeTask(index + 1).first()
      assertNotNull("${status.name} did not come back", stored)
      assertEquals(status, stored!!.status)
    }
  }
}
