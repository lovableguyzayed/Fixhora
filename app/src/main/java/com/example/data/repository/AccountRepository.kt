package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.room.AppDatabase
import com.example.data.room.TaskStatus
import com.example.data.room.photoUriList

/**
 * Account-wide operations that touch more than one table.
 *
 * The privacy policy promises that deleting an account removes everything it posted; this is where
 * that promise is kept.
 */
class AccountRepository(private val database: AppDatabase) {

  /**
   * Deletes [userId]'s account and everything it posted, in one transaction, and returns the stored
   * photos those tasks held so the caller can remove the files, which live outside the database.
   *
   * Jobs this user had taken as a helper and not yet finished go back to being open, so the
   * customer who posted them is not left waiting on someone who no longer exists. Their thread goes
   * too: the next helper must not inherit a conversation with somebody else. Finished jobs are left
   * as they are; they are the customer's record of work that was done.
   */
  suspend fun deleteAccount(userId: String): List<String> =
    database.withTransaction {
      val taskDao = database.taskDao()
      val chatDao = database.chatDao()

      val owned = taskDao.allTasksOwnedBy(userId)
      owned.forEach { task ->
        chatDao.deleteMessagesForTask(task.id)
        taskDao.deleteTaskById(task.id)
      }

      taskDao
        .tasksAcceptedBy(userId)
        .filter { it.status.isActiveEngagement }
        .forEach { task ->
          chatDao.deleteMessagesForTask(task.id)
          taskDao.updateTask(task.copy(status = TaskStatus.SUBMITTED, acceptedByHelperId = null))
        }

      database.userDao().deleteById(userId)
      owned.flatMap { it.photoUriList() }
    }
}
