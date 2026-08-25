package com.example.ui.format

import com.example.data.room.TaskStatus

/**
 * What a task's fields *mean*, without deciding what they say.
 *
 * Every function here returns a decision — "this is four hours old", "no budget was given" — and
 * the UI turns that into words. That split exists for two reasons:
 *
 * 1. **Plurals.** "1 min ago" and "5 mins ago" differ in English, and differ again in Hindi. Only
 *    `pluralStringResource` gets that right, and it needs the number, not a finished sentence.
 * 2. **Testability.** This file has no Android imports, so its tests run on a plain JVM in
 *    milliseconds. Returning `R.string` ids or resolved strings would have dragged the resource
 *    system in here for nothing.
 *
 * The tests that used to assert English wording now assert the decision, which is the part that
 * ever had bugs: a clock-skew case reading "in -3 minutes", a card claiming "10 mins ago" whatever
 * the age, "2.5 km away" for a distance nobody tracked.
 */

/** Owner id used by the debug seeder; see `DemoDataSeeder`. */
private const val DEMO_OWNER_ID = "demo"

private const val GUEST_OWNER_ID = "guest"

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/** How old a task is, in the coarsest unit that still says something useful. */
sealed interface RelativeTime {
  data object JustNow : RelativeTime

  data class Minutes(val value: Int) : RelativeTime

  data class Hours(val value: Int) : RelativeTime

  data object Yesterday : RelativeTime

  data class Days(val value: Int) : RelativeTime

  data object OverAMonth : RelativeTime
}

/**
 * Age of a task, from its `createdAt`.
 *
 * A future timestamp reads as [RelativeTime.JustNow] rather than a negative duration: clock skew
 * between writing a row and reading it back should not produce "in -3 minutes".
 */
fun relativeTime(createdAt: Long, now: Long): RelativeTime {
  val elapsed = now - createdAt
  return when {
    elapsed < MINUTE -> RelativeTime.JustNow
    elapsed < HOUR -> RelativeTime.Minutes((elapsed / MINUTE).toInt())
    elapsed < DAY -> RelativeTime.Hours((elapsed / HOUR).toInt())
    elapsed < 2 * DAY -> RelativeTime.Yesterday
    elapsed < 30 * DAY -> RelativeTime.Days((elapsed / DAY).toInt())
    else -> RelativeTime.OverAMonth
  }
}

/**
 * Who posted a task.
 *
 * Every card used to read "Customer Name" with a verified tick and a 4.8 rating, none of which came
 * from anywhere. There is no verification and no rating system, so this reports only what is
 * actually known.
 */
sealed interface Poster {
  data class Named(val name: String) : Poster

  data object DemoRequest : Poster

  data object GuestRequest : Poster

  /** A real account whose name is not loaded, or an id nothing matches. */
  data object UnknownCustomer : Poster
}

fun poster(ownerId: String, ownerNames: Map<String, String>): Poster {
  val name = ownerNames[ownerId]
  return when {
    !name.isNullOrBlank() -> Poster.Named(name)
    ownerId == DEMO_OWNER_ID -> Poster.DemoRequest
    ownerId == GUEST_OWNER_ID -> Poster.GuestRequest
    else -> Poster.UnknownCustomer
  }
}

/** What the customer said they would pay, or an honest note that they said nothing. */
sealed interface Budget {
  data class Range(val min: String, val max: String) : Budget

  data class From(val min: String) : Budget

  data class UpTo(val max: String) : Budget

  data object NotSet : Budget
}

fun budget(minBudget: String, maxBudget: String): Budget {
  val min = minBudget.trim()
  val max = maxBudget.trim()
  return when {
    min.isNotEmpty() && max.isNotEmpty() -> Budget.Range(min, max)
    min.isNotEmpty() -> Budget.From(min)
    max.isNotEmpty() -> Budget.UpTo(max)
    else -> Budget.NotSet
  }
}

/**
 * Where the job is.
 *
 * The feed used to print "2.5 km away" for anything posted with the location switch on. Distance
 * needs the helper's own position, which the app does not track, so this reports the address the
 * customer actually gave.
 */
sealed interface TaskLocation {
  data class Address(val text: String) : TaskLocation

  data object PinnedOnMap : TaskLocation

  data object NotGiven : TaskLocation
}

fun taskLocation(locationQuery: String, hasCoordinates: Boolean): TaskLocation =
  when {
    locationQuery.isNotBlank() -> TaskLocation.Address(locationQuery)
    hasCoordinates -> TaskLocation.PinnedOnMap
    else -> TaskLocation.NotGiven
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
