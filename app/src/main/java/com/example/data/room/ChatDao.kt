package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getMessagesForTask(taskId: Int): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_messages WHERE taskId = :taskId")
    suspend fun deleteMessagesForTask(taskId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}
