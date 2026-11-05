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
  /**
   * Helpers who chose "Not for me", joined by [ID_SEPARATOR]. Declining hides the task from that
   * helper only; the task stays open for everyone else.
   */
  val declinedBy: String? = null,
  /** The day the customer wants the work done, as an epoch day; null when they did not say. */
  val scheduledDate: Long? = null,
  /** Part of that day — one of the [TimeSlot] storage values; null when they did not say. */
  val timeSlot: String? = null,
  /** House or flat number and building. Shown to a helper only once they have taken the job. */
  val addressLine: String? = null,
  val landmark: String? = null,
  /** Why the customer called the task off, when they did. */
  val cancelReason: String? = null,
  /** The customer's 1–5 rating of the helper, given when they confirm the job is done. */
  val rating: Int? = null,
  val review: String? = null,
  /** When the customer confirmed the work was done; null until then. */
  val confirmedAt: Long? = null,
) {
  companion object {
    /** Owner id used for tasks created before signing in ("Continue as Guest"). */
    const val GUEST_OWNER_ID = "guest"

    const val PHOTO_URI_SEPARATOR = "\n"

    /** Separator for [declinedBy]. Account ids are UUIDs or [GUEST_OWNER_ID], never a newline. */
    const val ID_SEPARATOR = "\n"
  }
}

/** Ids of the helpers who declined this task. */
fun TaskEntity.declinedHelperIds(): Set<String> =
  declinedBy.orEmpty().split(TaskEntity.ID_SEPARATOR).filter { it.isNotBlank() }.toSet()

/** This task with [helperId] added to the helpers who declined it. */
fun TaskEntity.declinedByHelper(helperId: String): TaskEntity =
  copy(declinedBy = (declinedHelperIds() + helperId).joinToString(TaskEntity.ID_SEPARATOR))

/** Splits [TaskEntity.photoUris] back into individual URIs, dropping empties. */
fun TaskEntity.photoUriList(): List<String> =
  photoUris.split(TaskEntity.PHOTO_URI_SEPARATOR).filter { it.isNotBlank() }

/** Joins photo URIs for storage in [TaskEntity.photoUris]. */
fun List<String>.joinPhotoUris(): String =
  filter { it.isNotBlank() }.joinToString(TaskEntity.PHOTO_URI_SEPARATOR)
