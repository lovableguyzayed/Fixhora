package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE status != 'draft' ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY id DESC")
    fun getTasksByStatus(status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'draft' LIMIT 1")
    suspend fun getDraftTask(): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Query("SELECT * FROM tasks WHERE status != 'draft' AND ownerId = :ownerId ORDER BY id DESC")
    fun tasksOwnedBy(ownerId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: Int): TaskEntity?

    /** Tasks posted as a guest become this person's after they log in. */
    @Query("UPDATE tasks SET ownerId = :ownerId WHERE ownerId = 'guest'")
    suspend fun claimGuestTasks(ownerId: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
