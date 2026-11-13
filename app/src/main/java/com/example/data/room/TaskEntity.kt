package com.example.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val status: String, // "draft" or "submitted"
    val categoryId: String?,
    val locationQuery: String,
    val useCurrentLocation: Boolean,
    val selectedDistance: Int,
    val descriptionTitle: String,
    val descriptionDetails: String,
    val minBudget: String,
    val maxBudget: String,
    val photoUris: String, // Stored as comma-separated string for simplicity
    /** Account that posted the task ("guest" when posted without login; null for sample jobs). */
    val ownerId: String? = null,
    /** When the task was posted (epoch millis). 0 for tasks saved before version 3. */
    @ColumnInfo(defaultValue = "0") val createdAt: Long = 0L
)
