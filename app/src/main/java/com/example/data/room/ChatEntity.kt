package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    /** One of `ChatRepository.SENDER_WORKER` / `ChatRepository.SENDER_CUSTOMER`. */
    val senderId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "sent" // sent, delivered, read
)
