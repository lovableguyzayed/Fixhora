package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

  /** Every task that has actually been posted. Drafts are private to their author. */
  @Query("SELECT * FROM tasks WHERE status != 'draft' ORDER BY createdAt DESC, id DESC")
  fun getAllPostedTasks(): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC, id DESC")
  fun getTasksByStatus(status: TaskStatus): Flow<List<TaskEntity>>

  /** Number of posted tasks; used to decide whether demo data still needs seeding. */
  @Query("SELECT COUNT(*) FROM tasks WHERE status != 'draft'") suspend fun countPostedTasks(): Int

  /**
   * What one customer has posted, newest first.
   *
   * Drafts are excluded: an unfinished draft belongs to the post wizard, not to a list of things
   * the customer has actually asked for.
   */
  @Query("SELECT * FROM tasks WHERE ownerId = :ownerId AND status != 'draft' ORDER BY createdAt DESC, id DESC")
  fun getTasksForOwner(ownerId: String): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE id = :id") fun observeTask(id: Int): Flow<TaskEntity?>

  /** A customer has at most one draft in flight at a time. */
  @Query("SELECT * FROM tasks WHERE status = 'draft' AND ownerId = :ownerId LIMIT 1")
  suspend fun getDraftTask(ownerId: String): TaskEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTask(task: TaskEntity): Long

  @Update suspend fun updateTask(task: TaskEntity)

  @Query("DELETE FROM tasks WHERE id = :id") suspend fun deleteTaskById(id: Int)
}
