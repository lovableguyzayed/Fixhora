package com.example.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TaskEntity::class, ChatMessageEntity::class, OfferEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun chatDao(): ChatDao
    abstract fun offerDao(): OfferDao

    companion object {
        /** v2 -> v3: task owner + posted time, and the new offers table. Keeps all existing data. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `ownerId` TEXT")
                db.execSQL("ALTER TABLE `tasks` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `offers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`taskId` INTEGER NOT NULL, `helperId` TEXT NOT NULL, `helperName` TEXT NOT NULL, " +
                        "`amount` INTEGER NOT NULL, `message` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                        "`status` TEXT NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_offers_taskId` ON `offers` (`taskId`)")
            }
        }
    }
}
