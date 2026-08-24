package com.example.ui.format

import com.example.data.room.TaskStatus

/**
 * Text formatting for anything that renders a task.
 *
 * Lived in `ui/screens/helper` while only the worker screens showed tasks. The customer's own task
 * list needs the same labels, and one shared copy beats two that drift apart.
 *
 * Deliberately free of Android imports so it can be tested on a plain JVM. These replace hardcoded
 * strings — "10 mins ago", "Customer Name", "2.5 km away" — that were printed regardless of what
 * the task actually held.
 */

/** Owner id used by the debug seeder; see `DemoDataSeeder`. */
private const val DEMO_OWNER_ID = "demo"

private const val GUEST_OWNER_ID = "guest"

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/**
 * How long ago a task was posted, from its `createdAt`.
 *
 * A future timestamp reads as "Just now" rather than a negative duration: clock skew between
 * writing a row and reading it back should not produce "in -3 minutes".
 */
fun relativeTimeLabel(createdAt: Long, now: Long): String {
  val elapsed = now - createdAt
  return when {
    elapsed < MINUTE -> "Just now"
    elapsed < HOUR -> {
      val minutes = elapsed / MINUTE
      if (minutes == 1L) "1 min ago" else "$minutes mins ago"
    }
    elapsed < DAY -> {
      val hours = elapsed / HOUR
      if (hours == 1L) "1 hour ago" else "$hours hours ago"
    }
    elapsed < 30 * DAY -> {
      val days = elapsed / DAY
      if (days == 1L) "Yesterday" else "$days days ago"
    }
    else -> "Over a month ago"
  }
}

/**
 * Who posted the task.
 *
 * Every card used to read "Customer Name" with a verified tick and a 4.8 rating, none of which
 * came from anywhere. There is no verification and no rating system, so this reports only what is
 * actually known: the account's name, or that the task came from a guest or the demo seeder.
 */
fun posterLabel(ownerId: String, ownerNames: Map<String, String>): String =
  when {
    ownerNames[ownerId]?.isNotBlank() == true -> ownerNames.getValue(ownerId)
    ownerId == DEMO_OWNER_ID -> "Demo request"
    ownerId == GUEST_OWNER_ID -> "Guest request"
    else -> "Customer"
  }

/** The customer's budget, or an honest note that they did not give one. */
fun budgetLabel(minBudget: String, maxBudget: String): String {
  val min = minBudget.trim()
  val max = maxBudget.trim()
  return when {
    min.isNotEmpty() && max.isNotEmpty() -> "₹$min – ₹$max"
    min.isNotEmpty() -> "From ₹$min"
    max.isNotEmpty() -> "Up to ₹$max"
    else -> "No budget set"
  }
}

/**
 * Where the job is.
 *
 * The feed used to print "2.5 km away" for anything posted with the location switch on. Distance
 * needs the helper's own position, which the app does not track, so this shows the address the
 * customer actually gave.
 */
fun locationLabel(locationQuery: String, hasCoordinates: Boolean): String =
  when {
    locationQuery.isNotBlank() -> locationQuery
    hasCoordinates -> "Location pinned on map"
    else -> "No location given"
  }

/**
 * What a status means **to the customer who posted the task**.
 *
 * The stored `TaskStatus` names describe the row, not the situation: "SUBMITTED" tells a customer
 * nothing about whether anyone has picked their job up. These read as answers to "what is
 * happening with my task?".
 *
 * Kept separate from the worker's own labels on purpose — the same row means different things to
 * the two sides. `REJECTED` is "declined" to the worker who declined it, but to the customer it
 * simply means nobody has taken it yet.
 */
fun customerStatusLabel(status: TaskStatus): String =
  when (status) {
    TaskStatus.DRAFT -> "Draft"
    TaskStatus.SUBMITTED -> "Waiting for a helper"
    TaskStatus.ACCEPTED -> "Helper assigned"
    TaskStatus.IN_PROGRESS -> "Work in progress"
    TaskStatus.COMPLETED -> "Completed"
    TaskStatus.CANCELLED -> "Cancelled"
    TaskStatus.REJECTED -> "No helper yet"
  }

/**
 * One line of context under the status, so the label is not the only thing the customer gets.
 *
 * The old success screen promised "You will be notified once someone accepts your task", which was
 * never true — there are no notifications. These say what is actually the case.
 */
fun customerStatusDetail(status: TaskStatus): String =
  when (status) {
    TaskStatus.DRAFT -> "Not posted yet. Finish the steps to publish it."
    TaskStatus.SUBMITTED -> "Your task is visible to helpers. Check back to see if it was picked up."
    TaskStatus.ACCEPTED -> "A helper has taken this on. You can message them below."
    TaskStatus.IN_PROGRESS -> "The helper has started work."
    TaskStatus.COMPLETED -> "The helper marked this done."
    TaskStatus.CANCELLED -> "You cancelled this task."
    TaskStatus.REJECTED -> "A helper passed on this one. It is still open to everybody else."
  }

/**
 * Whether the customer can still call this task off.
 *
 * Once a helper has started, cancelling from the app would leave them working on something the
 * customer thinks is dead, and there is no channel to tell them beyond the chat — so the app does
 * not offer it.
 */
fun isCancellableByCustomer(status: TaskStatus): Boolean =
  status == TaskStatus.SUBMITTED || status == TaskStatus.REJECTED

/** Whether a conversation with a helper exists yet. Nobody is assigned before ACCEPTED. */
fun hasAssignedHelper(status: TaskStatus): Boolean =
  status == TaskStatus.ACCEPTED ||
    status == TaskStatus.IN_PROGRESS ||
    status == TaskStatus.COMPLETED
