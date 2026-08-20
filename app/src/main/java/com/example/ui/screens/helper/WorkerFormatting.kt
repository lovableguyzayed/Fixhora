package com.example.ui.screens.helper

/**
 * Text formatting for the worker screens.
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
