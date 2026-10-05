package com.example.data.room

/**
 * Lifecycle state of a task.
 *
 * [storageValue] is what Room persists. The strings deliberately match the free-form values that
 * were written before this enum existed ("draft", "submitted", "accepted", "completed",
 * "rejected"), so upgrading needs no data migration for the column itself.
 */
enum class TaskStatus(val storageValue: String) {
  /** Being composed by the customer; never visible to helpers. */
  DRAFT("draft"),

  /** Posted and open for helpers to pick up. */
  SUBMITTED("submitted"),

  /** A helper has taken the job but has not started it. */
  ACCEPTED("accepted"),

  /** The helper is actively working on it. */
  IN_PROGRESS("in_progress"),

  /** Work is finished. */
  COMPLETED("completed"),

  /** Withdrawn by the customer. */
  CANCELLED("cancelled"),

  /** Declined by a helper; stays out of that helper's feed. */
  REJECTED("rejected");

  /** True for states a helper can still act on. */
  val isOpenForHelpers: Boolean
    get() = this == SUBMITTED

  /** True for states that represent live work between a customer and a helper. */
  val isActiveEngagement: Boolean
    get() = this == ACCEPTED || this == IN_PROGRESS

  companion object {
    private val BY_STORAGE_VALUE: Map<String, TaskStatus> = entries.associateBy { it.storageValue }

    /**
     * Rows written before this enum existed can hold arbitrary text, so an unrecognised value
     * degrades to [CANCELLED] rather than throwing. [CANCELLED] is the conservative choice: it
     * keeps the row out of every feed instead of silently surfacing it as an actionable job or
     * as the customer's editable draft.
     */
    fun fromStorage(value: String?): TaskStatus =
      BY_STORAGE_VALUE[value?.trim()?.lowercase()] ?: CANCELLED
  }
}
