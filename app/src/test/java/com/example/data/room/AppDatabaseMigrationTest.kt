package com.example.data.room

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.io.File
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
 * Runs [AppDatabase.MIGRATION_2_3] against a real, populated v2 database.
 *
 * Until this existed, the migration had never executed anywhere. It is the one code path in the app
 * that can destroy a user's data: it rebuilds `tasks` by hand — CREATE / INSERT…SELECT / DROP /
 * RENAME — and hand-writes `users` and three indexes. Room compares what it finds at open time
 * against what it generated from the entities, and any disagreement throws
 * `IllegalStateException: Migration didn't properly handle…` **on the user's device, on launch,
 * with their data already committed to the new shape.** Release has no escape hatch:
 * `fallbackToDestructiveMigrationFrom` is restricted to v1 precisely so a bad migration surfaces as
 * an error rather than as silent data loss.
 *
 * Robolectric rather than instrumentation: CI has no emulator, so an `androidTest` version of this
 * would never run. The SDK is pinned because the SQLite behaviour under test does not vary with it,
 * and the first Room test in the project should not also be a bet on Robolectric's newest API
 * support.
 */
@RunWith(RobolectricTestRunner::class)
// A plain Application, not FixhoraApplication: the composition root opens the production database
// and seeds demo data in onCreate, which has no business running inside a database test.
@Config(sdk = [34], application = Application::class)
class AppDatabaseMigrationTest {

  private lateinit var context: Context
  private lateinit var databaseFile: File

  /** Whatever an installed v1.0.x build wrote before Batch 1 changed the schema. */
  private companion object {
    const val DB_NAME = "migration-test.db"

    val V2_TASKS =
      """
      CREATE TABLE IF NOT EXISTS `tasks` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `status` TEXT NOT NULL,
        `categoryId` TEXT,
        `locationQuery` TEXT NOT NULL,
        `useCurrentLocation` INTEGER NOT NULL,
        `selectedDistance` INTEGER NOT NULL,
        `descriptionTitle` TEXT NOT NULL,
        `descriptionDetails` TEXT NOT NULL,
        `minBudget` TEXT NOT NULL,
        `maxBudget` TEXT NOT NULL,
        `photoUris` TEXT NOT NULL
      )
      """

    val V2_CHAT =
      """
      CREATE TABLE IF NOT EXISTS `chat_messages` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        `taskId` INTEGER NOT NULL,
        `senderId` TEXT NOT NULL,
        `text` TEXT NOT NULL,
        `timestamp` INTEGER NOT NULL,
        `status` TEXT NOT NULL
      )
      """
  }

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    databaseFile = context.getDatabasePath(DB_NAME)
    databaseFile.parentFile?.mkdirs()
    databaseFile.delete()
  }

  @After
  fun tearDown() {
    databaseFile.delete()
  }

  /** Writes the schema a pre-Batch-1 build shipped, with rows in it, and marks it version 2. */
  private fun createPopulatedV2Database() {
    val db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null)
    db.execSQL(V2_TASKS.trimIndent())
    db.execSQL(V2_CHAT.trimIndent())

    // A submitted task carrying two comma-separated photo URIs — the v2 storage format.
    db.execSQL(
      """
      INSERT INTO tasks (id, status, categoryId, locationQuery, useCurrentLocation,
                         selectedDistance, descriptionTitle, descriptionDetails,
                         minBudget, maxBudget, photoUris)
      VALUES (1, 'submitted', 'repairs', 'Sector 62, Noida', 1, 5,
              'Leaking kitchen sink', 'Water under the cabinet', '500', '1500',
              'content://photo/1,content://photo/2')
      """
        .trimIndent()
    )
    // A draft with the optional fields left empty, which is what a half-filled wizard writes.
    db.execSQL(
      """
      INSERT INTO tasks (id, status, categoryId, locationQuery, useCurrentLocation,
                         selectedDistance, descriptionTitle, descriptionDetails,
                         minBudget, maxBudget, photoUris)
      VALUES (2, 'draft', NULL, '', 0, 5, '', '', '', '', '')
      """
        .trimIndent()
    )
    db.execSQL(
      """
      INSERT INTO chat_messages (id, taskId, senderId, text, timestamp, status)
      VALUES (1, 1, 'customer', 'Can you come today?', 1700000000000, 'sent')
      """
        .trimIndent()
    )

    db.version = 2
    db.close()
  }

  /**
   * Opens through the real builder used in production.
   *
   * Reading through a DAO matters: Room validates lazily, so building the database is not enough to
   * prove anything.
   */
  private fun openMigrated(): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
      .addMigrations(AppDatabase.MIGRATION_2_3)
      .build()

  /**
   * The whole point of the batch: the migrated schema has to match what Room generated from the
   * entities, or the app crashes on launch for anyone upgrading.
   */
  @Test
  fun `migrating a populated v2 database produces a schema Room accepts`() = runBlocking {
    createPopulatedV2Database()

    val db = openMigrated()
    try {
      // Forces the open, the migration, and the schema validation.
      val tasks = db.taskDao().getAllPostedTasks().first()

      assertEquals(1, tasks.size)
      assertEquals(1, tasks.first().id)
    } finally {
      db.close()
    }
  }

  @Test
  fun `every task survives, keeping its id and its content`() = runBlocking {
    createPopulatedV2Database()

    val db = openMigrated()
    try {
      val draft = db.taskDao().getDraftTask(TaskEntity.GUEST_OWNER_ID)
      val posted = db.taskDao().getAllPostedTasks().first()

      assertEquals(1, posted.size)
      val task = posted.first()
      assertEquals(1, task.id)
      assertEquals(TaskStatus.SUBMITTED, task.status)
      assertEquals("Leaking kitchen sink", task.descriptionTitle)
      assertEquals("Sector 62, Noida", task.locationQuery)
      assertEquals("500", task.minBudget)
      assertEquals("1500", task.maxBudget)
      assertEquals("repairs", task.categoryId)
      assertTrue(task.useCurrentLocation)

      // The draft is not in the posted feed, but it is still there.
      assertNotNull("the draft was lost in the migration", draft)
      assertEquals(2, draft!!.id)
      assertEquals(TaskStatus.DRAFT, draft.status)
    } finally {
      db.close()
    }
  }

  /**
   * v2 joined photo URIs with a comma; the entity now splits on a newline. Without the REPLACE in
   * the migration, every migrated task would render its photos as one broken URI.
   */
  @Test
  fun `photo URIs are converted from comma separated to newline separated`() = runBlocking {
    createPopulatedV2Database()

    val db = openMigrated()
    try {
      val task = db.taskDao().getAllPostedTasks().first().first()

      assertEquals(listOf("content://photo/1", "content://photo/2"), task.photoUriList())
    } finally {
      db.close()
    }
  }

  /** New columns have to be filled with something usable, not left at zero. */
  @Test
  fun `migrated rows get the guest owner and a plausible creation time`() = runBlocking {
    val before = System.currentTimeMillis()
    createPopulatedV2Database()

    val db = openMigrated()
    try {
      val task = db.taskDao().getAllPostedTasks().first().first()

      assertEquals(TaskEntity.GUEST_OWNER_ID, task.ownerId)
      assertNull(task.acceptedByHelperId)
      assertNull(task.latitude)
      assertNull(task.longitude)
      // Not 1970: the feed would otherwise show every pre-existing task as decades old.
      assertTrue(
        "createdAt was ${task.createdAt}, which is not a recent timestamp",
        task.createdAt >= before - 60_000,
      )
    } finally {
      db.close()
    }
  }

  /** `chat_messages` is untouched by the migration, so existing conversations must survive it. */
  @Test
  fun `existing chat messages are left alone`() = runBlocking {
    createPopulatedV2Database()

    val db = openMigrated()
    try {
      val messages = db.chatDao().getMessagesForTask(1).first()

      assertEquals(1, messages.size)
      assertEquals("Can you come today?", messages.first().text)
      assertEquals(1_700_000_000_000L, messages.first().timestamp)
    } finally {
      db.close()
    }
  }

  /** The migration creates `users` by hand; a usable account has to be insertable afterwards. */
  @Test
  fun `the users table is created and works`() = runBlocking {
    createPopulatedV2Database()

    val db = openMigrated()
    try {
      db.userDao().insert(sampleUser(id = "u1", mobile = "9876543210"))

      assertNotNull(db.userDao().findByMobile("9876543210"))
    } finally {
      db.close()
    }
  }

  /**
   * The unique index on `mobile` is what stops two accounts sharing a login. Asserting it exists by
   * name would not prove it is unique, so this proves it by violating it.
   */
  @Test(expected = SQLiteConstraintException::class)
  fun `two accounts cannot share a mobile number`(): Unit = runBlocking {
    createPopulatedV2Database()

    val db = openMigrated()
    try {
      db.userDao().insert(sampleUser(id = "u1", mobile = "9876543210"))
      db.userDao().insert(sampleUser(id = "u2", mobile = "9876543210"))
    } finally {
      db.close()
    }
  }

  private fun sampleUser(id: String, mobile: String) =
    UserEntity(
      id = id,
      fullName = "Anita Sharma",
      mobile = mobile,
      email = null,
      passwordHash = "hash",
      passwordSalt = "salt",
      gender = null,
      dateOfBirth = null,
      city = "Noida",
      state = "Uttar Pradesh",
      pinCode = "201309",
      preferredLanguage = "en",
      createdAt = 1_700_000_000_000L,
    )
}
