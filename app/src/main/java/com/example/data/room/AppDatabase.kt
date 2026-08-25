package com.example.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schemas are exported to `app/schemas` and committed.
 *
 * They were not, which is how `MIGRATION_2_3` came to be hand-written against no reference at all:
 * the shape Room expects existed only inside the generated code. With the JSON committed, a schema
 * change shows up in a diff and the next migration can be checked against it rather than guessed.
 */
@Database(
  entities = [TaskEntity::class, ChatMessageEntity::class, UserEntity::class],
  version = 3,
  exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun taskDao(): TaskDao

  abstract fun chatDao(): ChatDao

  abstract fun userDao(): UserDao

  companion object {
    const val DATABASE_NAME = "fixhora-database"

    /**
     * v2 -> v3: adds the `users` table, and gives `tasks` an owner, an accepting helper,
     * coordinates and a creation time.
     *
     * `tasks` is rebuilt rather than patched with `ALTER TABLE ... ADD COLUMN`, because an added
     * column needs a DEFAULT to satisfy NOT NULL, and a DEFAULT present in SQLite but absent from
     * the entity makes Room's schema validation fail at open time. Rebuilding produces exactly the
     * schema Room generates.
     *
     * Existing rows get [TaskEntity.GUEST_OWNER_ID] as their owner (they predate accounts) and
     * `now` as their creation time, so the helper feed shows a plausible age instead of 1970.
     * Stored photo URIs move from comma-separated to newline-separated to match the entity.
     */
    val MIGRATION_2_3 =
      object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `users` (
              `id` TEXT NOT NULL,
              `fullName` TEXT NOT NULL,
              `mobile` TEXT NOT NULL,
              `email` TEXT,
              `passwordHash` TEXT NOT NULL,
              `passwordSalt` TEXT NOT NULL,
              `gender` TEXT,
              `dateOfBirth` TEXT,
              `city` TEXT NOT NULL,
              `state` TEXT NOT NULL,
              `pinCode` TEXT NOT NULL,
              `preferredLanguage` TEXT NOT NULL,
              `createdAt` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """
              .trimIndent()
          )
          db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_mobile` ON `users` (`mobile`)")

          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `tasks_new` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `status` TEXT NOT NULL,
              `ownerId` TEXT NOT NULL,
              `acceptedByHelperId` TEXT,
              `categoryId` TEXT,
              `locationQuery` TEXT NOT NULL,
              `useCurrentLocation` INTEGER NOT NULL,
              `latitude` REAL,
              `longitude` REAL,
              `selectedDistance` INTEGER NOT NULL,
              `descriptionTitle` TEXT NOT NULL,
              `descriptionDetails` TEXT NOT NULL,
              `minBudget` TEXT NOT NULL,
              `maxBudget` TEXT NOT NULL,
              `photoUris` TEXT NOT NULL,
              `createdAt` INTEGER NOT NULL
            )
            """
              .trimIndent()
          )
          db.execSQL(
            """
            INSERT INTO `tasks_new` (
              `id`, `status`, `ownerId`, `acceptedByHelperId`, `categoryId`, `locationQuery`,
              `useCurrentLocation`, `latitude`, `longitude`, `selectedDistance`,
              `descriptionTitle`, `descriptionDetails`, `minBudget`, `maxBudget`, `photoUris`,
              `createdAt`
            )
            SELECT
              `id`, `status`, '${TaskEntity.GUEST_OWNER_ID}', NULL, `categoryId`, `locationQuery`,
              `useCurrentLocation`, NULL, NULL, `selectedDistance`,
              `descriptionTitle`, `descriptionDetails`, `minBudget`, `maxBudget`,
              REPLACE(`photoUris`, ',', char(10)),
              CAST(strftime('%s', 'now') AS INTEGER) * 1000
            FROM `tasks`
            """
              .trimIndent()
          )
          db.execSQL("DROP TABLE `tasks`")
          db.execSQL("ALTER TABLE `tasks_new` RENAME TO `tasks`")
          db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_status` ON `tasks` (`status`)")
          db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_ownerId` ON `tasks` (`ownerId`)")
        }
      }
  }
}
