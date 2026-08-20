package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.room.AppDatabase
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val database: AppDatabase) {

  private val taskDao = database.taskDao()

  /** Tasks a helper can still pick up. */
  val openTasks: Flow<List<TaskEntity>> = taskDao.getTasksByStatus(TaskStatus.SUBMITTED)

  /** Every posted task, in any state. Drafts stay private to their author. */
  val allPostedTasks: Flow<List<TaskEntity>> = taskDao.getAllPostedTasks()

  suspend fun getDraftTask(ownerId: String): TaskEntity? = taskDao.getDraftTask(ownerId)

  /**
   * Writes the in-progress draft, replacing the owner's previous one.
   *
   * Read-then-write runs inside a transaction so that two rapid autosaves cannot both miss the
   * existing draft and leave two rows behind.
   */
  suspend fun saveDraft(task: TaskEntity) {
    database.withTransaction {
      val existing = taskDao.getDraftTask(task.ownerId)
      val draft = task.copy(status = TaskStatus.DRAFT)
      if (existing == null) {
        taskDao.insertTask(draft)
      } else {
        taskDao.updateTask(draft.copy(id = existing.id, createdAt = existing.createdAt))
      }
    }
  }

  /**
   * Posts the task.
   *
   * The draft row is promoted in place rather than copied to a new row and deleted, so there is no
   * window in which the task exists twice, and no way for a failure between the two writes to
   * leave an orphaned draft behind.
   */
  suspend fun submitTask(task: TaskEntity) {
    database.withTransaction {
      val existing = taskDao.getDraftTask(task.ownerId)
      val posted = task.copy(status = TaskStatus.SUBMITTED)
      if (existing == null) {
        taskDao.insertTask(posted.copy(createdAt = System.currentTimeMillis()))
      } else {
        taskDao.updateTask(posted.copy(id = existing.id, createdAt = System.currentTimeMillis()))
      }
    }
  }

  suspend fun discardDraft(ownerId: String) {
    database.withTransaction { taskDao.getDraftTask(ownerId)?.let { taskDao.deleteTaskById(it.id) } }
  }

  suspend fun updateTaskStatus(task: TaskEntity, newStatus: TaskStatus) {
    taskDao.updateTask(task.copy(status = newStatus))
  }
}
