package com.example.data.repository

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.room.AppDatabase
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
 * The draft transactions, which were written for a real bug and never tested.
 *
 * Batch 1 wrapped `saveDraft` and `submitTask` in `withTransaction` because the autosave is
 * debounced and fires repeatedly: a read-then-write without a transaction lets two saves both miss
 * the existing draft and insert, leaving the customer with two half-written tasks. Batch 3 fixed a
 * related bug where a late autosave recreated a draft *after* submit. None of it had coverage.
 */
@RunWith(RobolectricTestRunner::class)
// A plain Application, not FixhoraApplication: the composition root opens the production database
// and seeds demo data in onCreate, which has no business running inside a database test.
@Config(sdk = [34], application = Application::class)
class TaskRepositoryTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: TaskRepository

  private val owner = "anita"

  @Before
  fun setUp() {
    val context: Context = ApplicationProvider.getApplicationContext()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    repository = TaskRepository(database)
  }

  @After
  fun tearDown() {
    database.close()
  }

  private fun draft(title: String, ownerId: String = owner) =
    TaskEntity(
      status = TaskStatus.DRAFT,
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
    )

  private suspend fun allRows() = database.taskDao().getAllPostedTasks().first()

  /**
   * Every row, drafts included.
   *
   * Counted with a raw query rather than by adding a DAO method: production code should not grow a
   * function that exists only so a test can look at it.
   */
  private fun countAllRows(): Int =
    database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM tasks").use { cursor ->
      cursor.moveToFirst()
      cursor.getInt(0)
    }

  @Test
  fun `repeated saves keep exactly one draft and the latest content`() = runBlocking {
    repository.saveDraft(draft("Leak"))
    repository.saveDraft(draft("Leaking"))
    repository.saveDraft(draft("Leaking kitchen sink"))

    val stored = repository.getDraftTask(owner)

    assertNotNull(stored)
    assertEquals("Leaking kitchen sink", stored!!.descriptionTitle)
    // One draft row, not three.
    assertEquals(0, allRows().size)
    assertEquals(1, countAllRows())
  }

  /**
   * Overlapping autosaves must not both decide there is no draft yet.
   *
   * Honest about what this proves: `runBlocking` gives one thread, so this exercises interleaving
   * at suspension points rather than true parallelism. It is a regression guard on the
   * read-then-write shape, not a proof of thread safety — a real race needs a device.
   */
  @Test
  fun `concurrent saves still leave one draft`() = runBlocking {
    coroutineScope {
        listOf(
            async { repository.saveDraft(draft("A")) },
            async { repository.saveDraft(draft("B")) },
            async { repository.saveDraft(draft("C")) },
          )
          .awaitAll()
      }

    assertEquals(1, countAllRows())
  }

  @Test
  fun `submitting promotes the draft in place rather than adding a row`() = runBlocking {
    repository.saveDraft(draft("Leaking kitchen sink"))
    val draftId = repository.getDraftTask(owner)!!.id

    repository.submitTask(draft("Leaking kitchen sink"))

    val posted = allRows()
    assertEquals(1, posted.size)
    assertEquals("the draft was copied instead of promoted", draftId, posted.first().id)
    assertEquals(TaskStatus.SUBMITTED, posted.first().status)
    // Nothing left behind for the wizard to pick up next time.
    assertNull(repository.getDraftTask(owner))
  }

  @Test
  fun `submitting without a draft still posts the task`() = runBlocking {
    repository.submitTask(draft("Paint one wall"))

    val posted = allRows()
    assertEquals(1, posted.size)
    assertEquals("Paint one wall", posted.first().descriptionTitle)
  }

  @Test
  fun `a submitted task gets a real creation time`() = runBlocking {
    val before = System.currentTimeMillis()

    repository.submitTask(draft("Deep clean flat"))

    assertTrue(allRows().first().createdAt >= before)
  }

  @Test
  fun `discarding removes the draft and nothing else`() = runBlocking {
    repository.submitTask(draft("Already posted"))
    repository.saveDraft(draft("Still being written"))

    repository.discardDraft(owner)

    assertNull(repository.getDraftTask(owner))
    assertEquals(listOf("Already posted"), allRows().map { it.descriptionTitle })
  }

  /** One customer's autosave must never touch another customer's draft. */
  @Test
  fun `drafts are per owner`() = runBlocking {
    repository.saveDraft(draft("Anita's task", ownerId = "anita"))
    repository.saveDraft(draft("Bharat's task", ownerId = "bharat"))

    assertEquals("Anita's task", repository.getDraftTask("anita")?.descriptionTitle)
    assertEquals("Bharat's task", repository.getDraftTask("bharat")?.descriptionTitle)
    assertEquals(2, countAllRows())
  }

  @Test
  fun `discarding one owner's draft leaves another's alone`() = runBlocking {
    repository.saveDraft(draft("Anita's task", ownerId = "anita"))
    repository.saveDraft(draft("Bharat's task", ownerId = "bharat"))

    repository.discardDraft("anita")

    assertNull(repository.getDraftTask("anita"))
    assertNotNull(repository.getDraftTask("bharat"))
  }

  @Test
  fun `updating a status writes it and keeps the row`() = runBlocking {
    repository.submitTask(draft("Fix ceiling fan"))
    val task = allRows().first()

    repository.updateTaskStatus(task, TaskStatus.ACCEPTED)

    assertEquals(TaskStatus.ACCEPTED, database.taskDao().observeTask(task.id).first()?.status)
    assertEquals(1, countAllRows())
  }

  @Test
  fun `a customer only sees their own posted tasks`() = runBlocking {
    repository.submitTask(draft("Anita's task", ownerId = "anita"))
    repository.submitTask(draft("Bharat's task", ownerId = "bharat"))

    assertEquals(
      listOf("Anita's task"),
      repository.tasksForOwner("anita").first().map { it.descriptionTitle },
    )
  }
}
