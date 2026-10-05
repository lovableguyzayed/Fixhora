package com.example.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A task posted by a customer, or the single in-progress draft they are still composing.
 *
 * Indexed on [status] and [ownerId] because every feed query filters on one or both.
 */
@Entity(tableName = "tasks", indices = [Index("status"), Index("ownerId")])
data class TaskEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val status: TaskStatus,
  /** Id of the [UserEntity] who posted this, or [GUEST_OWNER_ID] when nobody is signed in. */
  val ownerId: String,
  /** Id of the [UserEntity] who took the job, once one has. */
  val acceptedByHelperId: String? = null,
  val categoryId: String?,
  val locationQuery: String,
  val useCurrentLocation: Boolean,
  /** Resolved coordinates, present only once location has actually been fetched. */
  val latitude: Double? = null,
  val longitude: Double? = null,
  val selectedDistance: Int,
  val descriptionTitle: String,
  val descriptionDetails: String,
  val minBudget: String,
  val maxBudget: String,
  /**
   * Photo URIs joined by [PHOTO_URI_SEPARATOR]. A newline is used rather than a comma because a
   * comma is legal inside a URI and would corrupt the split.
   */
  val photoUris: String,
  val createdAt: Long = System.currentTimeMillis(),
) {
  companion object {
    /** Owner id used for tasks created before signing in ("Continue as Guest"). */
    const val GUEST_OWNER_ID = "guest"

    const val PHOTO_URI_SEPARATOR = "\n"
  }
}

/** Splits [TaskEntity.photoUris] back into individual URIs, dropping empties. */
fun TaskEntity.photoUriList(): List<String> =
  photoUris.split(TaskEntity.PHOTO_URI_SEPARATOR).filter { it.isNotBlank() }

/** Joins photo URIs for storage in [TaskEntity.photoUris]. */
fun List<String>.joinPhotoUris(): String =
  filter { it.isNotBlank() }.joinToString(TaskEntity.PHOTO_URI_SEPARATOR)
