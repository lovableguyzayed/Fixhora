package com.example.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.room.TaskStatus

/**
 * The words for the decisions in `TaskFormatting.kt`.
 *
 * Kept in a separate file so that one stays free of Android imports and its tests keep running on
 * a plain JVM. Everything here is a lookup: no logic, nothing to get wrong twice.
 */

/**
 * How long ago, in words.
 *
 * The counted cases go through `pluralStringResource` rather than string concatenation, because
 * "1 min" and "5 mins" differ in English and differ again — by different rules — in Hindi. Passing
 * the count both as the quantity and as the format argument is required: the first selects the
 * form, the second fills the `%d`.
 */
@Composable
fun RelativeTime.text(): String =
  when (this) {
    RelativeTime.JustNow -> stringResource(R.string.time_just_now)
    is RelativeTime.Minutes -> pluralStringResource(R.plurals.minutes_ago, value, value)
    is RelativeTime.Hours -> pluralStringResource(R.plurals.hours_ago, value, value)
    RelativeTime.Yesterday -> stringResource(R.string.time_yesterday)
    is RelativeTime.Days -> pluralStringResource(R.plurals.days_ago, value, value)
    RelativeTime.OverAMonth -> stringResource(R.string.time_over_a_month)
  }

/** Convenience for the common case of "how old is this, right now". */
@Composable
fun relativeTimeText(createdAt: Long, now: Long = System.currentTimeMillis()): String =
  relativeTime(createdAt, now).text()

@Composable
fun Poster.text(): String =
  when (this) {
    is Poster.Named -> name
    Poster.DemoRequest -> stringResource(R.string.poster_demo)
    Poster.GuestRequest -> stringResource(R.string.poster_guest)
    Poster.UnknownCustomer -> stringResource(R.string.poster_customer)
  }

@Composable
fun posterText(ownerId: String, ownerNames: Map<String, String>): String =
  poster(ownerId, ownerNames).text()

@Composable
fun Budget.text(): String =
  when (this) {
    is Budget.Range -> stringResource(R.string.budget_range, min, max)
    is Budget.From -> stringResource(R.string.budget_from, min)
    is Budget.UpTo -> stringResource(R.string.budget_up_to, max)
    Budget.NotSet -> stringResource(R.string.budget_not_set)
  }

@Composable fun budgetText(minBudget: String, maxBudget: String): String =
  budget(minBudget, maxBudget).text()

@Composable
fun TaskLocation.text(): String =
  when (this) {
    is TaskLocation.Address -> text
    TaskLocation.PinnedOnMap -> stringResource(R.string.location_pinned)
    TaskLocation.NotGiven -> stringResource(R.string.location_not_given)
  }

@Composable
fun taskLocationText(locationQuery: String, hasCoordinates: Boolean): String =
  taskLocation(locationQuery, hasCoordinates).text()

/**
 * What a status means **to the customer who posted the task**.
 *
 * The stored names describe the row, not the situation: "SUBMITTED" tells a customer nothing about
 * whether anyone picked their job up. Deliberately separate from the worker's own vocabulary —
 * `REJECTED` is "declined" to the worker who declined it, but to the customer it only means nobody
 * has taken it yet.
 */
@Composable
fun customerStatusLabel(status: TaskStatus): String =
  stringResource(
    when (status) {
      TaskStatus.DRAFT -> R.string.status_draft
      TaskStatus.SUBMITTED -> R.string.status_waiting
      TaskStatus.ACCEPTED -> R.string.status_helper_assigned
      TaskStatus.IN_PROGRESS -> R.string.status_in_progress
      TaskStatus.COMPLETED -> R.string.status_completed
      TaskStatus.CANCELLED -> R.string.status_cancelled
      TaskStatus.REJECTED -> R.string.status_no_helper_yet
    }
  )

/**
 * One line of context under the status.
 *
 * The old success screen promised "You will be notified once someone accepts your task", which was
 * never true — there are no notifications. These say what is actually the case.
 */
@Composable
fun customerStatusDetail(status: TaskStatus): String =
  stringResource(
    when (status) {
      TaskStatus.DRAFT -> R.string.status_detail_draft
      TaskStatus.SUBMITTED -> R.string.status_detail_waiting
      TaskStatus.ACCEPTED -> R.string.status_detail_assigned
      TaskStatus.IN_PROGRESS -> R.string.status_detail_in_progress
      TaskStatus.COMPLETED -> R.string.status_detail_completed
      TaskStatus.CANCELLED -> R.string.status_detail_cancelled
      TaskStatus.REJECTED -> R.string.status_detail_rejected
    }
  )
