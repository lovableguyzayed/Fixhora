package com.example.data.repository

import com.example.data.room.ChatDao
import com.example.data.room.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    fun getMessagesForTask(taskId: Int): Flow<List<ChatMessageEntity>> = chatDao.getMessagesForTask(taskId)

    suspend fun sendMessage(taskId: Int, senderId: String, text: String) {
        chatDao.insertMessage(
            ChatMessageEntity(
                taskId = taskId,
                senderId = senderId,
                text = text
            )
        )
    }
}
