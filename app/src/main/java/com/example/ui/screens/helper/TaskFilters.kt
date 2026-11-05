package com.example.ui.screens.helper

import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.data.room.declinedHelperIds

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
 * a plain JVM. This is the same shape `poster(ownerId, ownerNames)` already uses.
 */
fun TaskEntity.matchesQuery(query: String, categoryTitles: Map<String, String> = emptyMap()): Boolean {
  val trimmed = query.trim()
  if (trimmed.isEmpty()) return true
  val categoryTitle = categoryTitles[categoryId].orEmpty()
  return listOf(descriptionTitle, descriptionDetails, locationQuery, categoryTitle).any {
    it.contains(trimmed, ignoreCase = true)
  }
}

/**
 * Still open, and not turned down by this helper.
 *
 * `REJECTED` counts as open: builds before v4 marked a task `REJECTED` when one helper declined,
 * which hid it from everybody. Declining is now recorded per helper in `declinedBy` instead.
 */
fun TaskEntity.isOpenFor(helperId: String): Boolean =
  (status == TaskStatus.SUBMITTED || status == TaskStatus.REJECTED) && !isDeclinedBy(helperId)

/**
 * Taken by this helper. Builds before v4 never recorded who accepted a task, and on the one phone
 * the app runs on such a task can only have been taken by whoever is using it now.
 */
fun TaskEntity.isTakenBy(helperId: String): Boolean =
  (status.isActiveEngagement || status == TaskStatus.COMPLETED) &&
    (acceptedByHelperId == null || acceptedByHelperId == helperId)

/** Turned down by this helper — or, for a pre-v4 `REJECTED` row that recorded nobody, by whoever declined it on this phone. */
fun TaskEntity.isDeclinedBy(helperId: String): Boolean =
  helperId in declinedHelperIds() || (status == TaskStatus.REJECTED && declinedBy == null)

/**
 * Applies the selected tab and the search box together.
 *
 * With a [helperId], each tab shows that helper's view: open jobs they have not declined, jobs they
 * took, and jobs they turned down. Without one it filters on status alone.
 */
fun List<TaskEntity>.filterFor(
  tab: TaskTab,
  query: String,
  categoryTitles: Map<String, String> = emptyMap(),
  helperId: String? = null,
): List<TaskEntity> =
  filter { task ->
    val inTab =
      if (helperId == null) {
        tab.status == null || task.status == tab.status
      } else {
        when (tab) {
          TaskTab.ALL -> task.isOpenFor(helperId) || task.isTakenBy(helperId) || task.isDeclinedBy(helperId)
          TaskTab.NEW -> task.isOpenFor(helperId)
          TaskTab.DECLINED -> task.isDeclinedBy(helperId) && !task.isTakenBy(helperId)
          else -> task.isTakenBy(helperId) && task.status == tab.status
        }
      }
    inTab && task.matchesQuery(query, categoryTitles)
  }

/** Tasks with a live engagement, which are the ones a helper can be in a conversation about. */
fun List<TaskEntity>.conversations(): List<TaskEntity> =
  filter { it.status.isActiveEngagement || it.status == TaskStatus.COMPLETED }

/**
 * This helper's conversations: jobs they took, and open jobs they have asked a question about —
 * [withMessages] being the ids of tasks that have any message at all.
 */
fun List<TaskEntity>.conversationsFor(helperId: String, withMessages: Set<Int>): List<TaskEntity> =
  filter { it.isTakenBy(helperId) || (it.isOpenFor(helperId) && it.id in withMessages) }

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

/** One helper's own numbers: what is open to them, and what they took or turned down. */
fun List<TaskEntity>.toHelperStats(helperId: String) =
  HelperStats(
    availableNow = count { it.isOpenFor(helperId) },
    accepted = count { it.isTakenBy(helperId) && it.status == TaskStatus.ACCEPTED },
    inProgress = count { it.isTakenBy(helperId) && it.status == TaskStatus.IN_PROGRESS },
    completed = count { it.isTakenBy(helperId) && it.status == TaskStatus.COMPLETED },
    declined = count { it.isDeclinedBy(helperId) && !it.isTakenBy(helperId) },
  )
