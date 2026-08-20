package com.example.ui.screens.helper

import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.ui.screens.taskflow.dummyCategories

/**
 * The tabs on the worker's task list, each backed by a real [TaskStatus].
 *
 * Previously the tabs were plain strings matched in a `when` that fell through to `else -> false`,
 * so "In Progress" and "Cancelled" could never show anything, and a "Pending" tab existed for a
 * status no task ever had.
 */
enum class TaskTab(val label: String, val status: TaskStatus?) {
  ALL("All", null),
  NEW("New", TaskStatus.SUBMITTED),
  ACCEPTED("Accepted", TaskStatus.ACCEPTED),
  IN_PROGRESS("In Progress", TaskStatus.IN_PROGRESS),
  COMPLETED("Completed", TaskStatus.COMPLETED),
  DECLINED("Declined", TaskStatus.REJECTED),
}

/**
 * Whether a task matches a free-text search.
 *
 * Searches everything a helper would plausibly type: the title, the details, the address and the
 * category name. The search box used to accept input and filter nothing at all.
 */
fun TaskEntity.matchesQuery(query: String): Boolean {
  val trimmed = query.trim()
  if (trimmed.isEmpty()) return true
  val categoryTitle = dummyCategories.find { it.id == categoryId }?.title.orEmpty()
  return listOf(descriptionTitle, descriptionDetails, locationQuery, categoryTitle).any {
    it.contains(trimmed, ignoreCase = true)
  }
}

/** Applies the selected tab and the search box together. */
fun List<TaskEntity>.filterFor(tab: TaskTab, query: String): List<TaskEntity> =
  filter { (tab.status == null || it.status == tab.status) && it.matchesQuery(query) }

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
