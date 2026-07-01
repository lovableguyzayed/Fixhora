package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val senderId: String, // "worker" or "customer"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "sent" // sent, delivered, read
)
