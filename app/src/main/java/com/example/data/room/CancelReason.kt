package com.example.data.room

/**
 * Why a customer called a task off, stored in [TaskEntity.cancelReason].
 *
 * A fixed list rather than free text: one tap is enough at a moment when the customer is already
 * leaving, and a stable key keeps meaning the same thing whichever language the app is in.
 */
enum class CancelReason(val storageValue: String) {
  FOUND_SOMEONE("found_someone"),
  NOT_NEEDED("not_needed"),
  NO_RESPONSE("no_response"),
  WRONG_DETAILS("wrong_details"),
  OTHER("other");

  companion object {
    private val BY_STORAGE_VALUE = entries.associateBy { it.storageValue }

    fun fromStorage(value: String?): CancelReason? = value?.let { BY_STORAGE_VALUE[it] }
  }
}
