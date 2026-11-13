package com.example.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A helper's offer on a task: price in whole rupees and an optional message. */
@Entity(tableName = "offers", indices = [Index("taskId")])
data class OfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val helperId: String,
    val helperName: String,
    val amount: Long,
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    /** "sent", "accepted" or "declined" */
    val status: String = "sent"
)
