package com.example.ui.screens.helper

import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus

/**
 * The tabs on the worker's task list, each backed by a real [TaskStatus].
 *
 * Previously the tabs were plain strings matched in a `when` that fell through to `else -> false`,
 * so "In Progress" and "Cancelled" could never show anything, and a "Pending" tab existed for a
 * status no task ever had.
 */
enum class TaskTab(val status: TaskStatus?) {
  ALL(null),
  NEW(TaskStatus.SUBMITTED),
  ACCEPTED(TaskStatus.ACCEPTED),
  IN_PROGRESS(TaskStatus.IN_PROGRESS),
  COMPLETED(TaskStatus.COMPLETED),
  DECLINED(TaskStatus.REJECTED),
}

/**
 * Whether a task matches a free-text search.
 *
 * Searches everything a helper would plausibly type: the title, the details, the address and the
 * category name. The search box used to accept input and filter nothing at all.
 *
 * [categoryTitles] maps a category id to its name **in the language currently on screen**, and is
 * supplied by the caller rather than looked up here. Reaching into `dummyCategories` for the title
 * would have meant matching English words while the user reads and types Hindi — and it would have
 * dragged Android resources into a file kept deliberately free of them so it can be unit-tested on
 * a plain JVM. This is the same shape `posterLabel(ownerId, ownerNames)` already uses.
 */
fun TaskEntity.matchesQuery(query: String, categoryTitles: Map<String, String> = emptyMap()): Boolean {
  val trimmed = query.trim()
  if (trimmed.isEmpty()) return true
  val categoryTitle = categoryTitles[categoryId].orEmpty()
  return listOf(descriptionTitle, descriptionDetails, locationQuery, categoryTitle).any {
    it.contains(trimmed, ignoreCase = true)
  }
}

/** Applies the selected tab and the search box together. */
fun List<TaskEntity>.filterFor(
  tab: TaskTab,
  query: String,
  categoryTitles: Map<String, String> = emptyMap(),
): List<TaskEntity> =
  filter { (tab.status == null || it.status == tab.status) && it.matchesQuery(query, categoryTitles) }

/** Tasks with a live engagement, which are the ones a helper can be in a conversation about. */
fun List<TaskEntity>.conversations(): List<TaskEntity> =
  filter { it.status.isActiveEngagement || it.status == TaskStatus.COMPLETED }

/** Counts per status, for the summary row. */
data class HelperStats(
  val availableNow: Int = 0,
  val accepted: Int = 0,
  val inProgress: Int = 0,
  val completed: Int = 0,
  val declined: Int = 0,
) {
  val activeNow: Int
    get() = accepted + inProgress
}

fun List<TaskEntity>.toHelperStats() =
  HelperStats(
    availableNow = count { it.status == TaskStatus.SUBMITTED },
    accepted = count { it.status == TaskStatus.ACCEPTED },
    inProgress = count { it.status == TaskStatus.IN_PROGRESS },
    completed = count { it.status == TaskStatus.COMPLETED },
    declined = count { it.status == TaskStatus.REJECTED },
  )
