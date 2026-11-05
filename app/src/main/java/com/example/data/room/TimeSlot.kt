package com.example.data.room

/**
 * The part of the day a customer wants the work done, stored as [storageValue] in
 * [TaskEntity.timeSlot].
 *
 * Coarse on purpose: "evening" is how people in India book a plumber or an electrician, and it
 * leaves the helper room to fit the visit in. The exact hour is agreed in the chat.
 */
enum class TimeSlot(val storageValue: String) {
  MORNING("morning"),
  AFTERNOON("afternoon"),
  EVENING("evening"),
  ANY_TIME("any_time");

  companion object {
    fun fromStorage(value: String?): TimeSlot? = entries.firstOrNull { it.storageValue == value }
  }
}
