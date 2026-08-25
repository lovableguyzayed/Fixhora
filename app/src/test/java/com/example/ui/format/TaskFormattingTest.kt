package com.example.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * These assert the *decision*, not the wording.
 *
 * The wording now lives in resources and differs by language, so asserting English here would
 * only pin one translation. The decision is the part that ever had bugs: a card reading
 * "10 mins ago" whatever its age, clock skew producing "in -3 minutes", "2.5 km away" for a
 * distance nobody tracked, and a fabricated name with a rating behind it.
 */
class TaskFormattingTest {

  private val now = 1_700_000_000_000L
  private val minute = 60_000L
  private val hour = 60 * minute
  private val day = 24 * hour

  @Test
  fun `age is reported in the coarsest unit that still says something`() {
    assertEquals(RelativeTime.JustNow, relativeTime(now, now))
    assertEquals(RelativeTime.JustNow, relativeTime(now - 30_000, now))
    assertEquals(RelativeTime.Minutes(1), relativeTime(now - minute, now))
    assertEquals(RelativeTime.Minutes(5), relativeTime(now - 5 * minute, now))
    assertEquals(RelativeTime.Hours(1), relativeTime(now - hour, now))
    assertEquals(RelativeTime.Hours(5), relativeTime(now - 5 * hour, now))
    assertEquals(RelativeTime.Yesterday, relativeTime(now - day, now))
    assertEquals(RelativeTime.Days(3), relativeTime(now - 3 * day, now))
    assertEquals(RelativeTime.OverAMonth, relativeTime(now - 60 * day, now))
  }

  /** The boundary between "Yesterday" and a day count. */
  @Test
  fun `yesterday covers the whole of the previous day and no more`() {
    assertEquals(RelativeTime.Yesterday, relativeTime(now - day, now))
    assertEquals(RelativeTime.Yesterday, relativeTime(now - (2 * day - 1), now))
    assertEquals(RelativeTime.Days(2), relativeTime(now - 2 * day, now))
  }

  /**
   * Counts are kept separate from the words so a plural rule can apply. English distinguishes 1
   * from 5; Hindi distinguishes them differently. Neither works if the number is already baked
   * into a sentence.
   */
  @Test
  fun `counted cases carry the number`() {
    assertEquals(1, (relativeTime(now - minute, now) as RelativeTime.Minutes).value)
    assertEquals(59, (relativeTime(now - 59 * minute, now) as RelativeTime.Minutes).value)
    assertEquals(23, (relativeTime(now - 23 * hour, now) as RelativeTime.Hours).value)
    assertEquals(29, (relativeTime(now - 29 * day, now) as RelativeTime.Days).value)
  }

  /** Clock skew must not produce a negative duration. */
  @Test
  fun `a future timestamp reads as just now`() {
    assertEquals(RelativeTime.JustNow, relativeTime(now + 5 * minute, now))
  }

  /** Was "Customer Name" plus a verified tick and a 4.8 rating, none of it backed by data. */
  @Test
  fun `poster reports only what is known`() {
    assertEquals(Poster.Named("Anita Sharma"), poster("u1", mapOf("u1" to "Anita Sharma")))
    assertEquals(Poster.DemoRequest, poster("demo", emptyMap()))
    assertEquals(Poster.GuestRequest, poster("guest", emptyMap()))
    assertEquals(Poster.UnknownCustomer, poster("unknown-id", mapOf("u1" to "Anita")))
    assertEquals(Poster.UnknownCustomer, poster("u1", mapOf("u1" to "")))
  }

  @Test
  fun `budget covers every combination the customer can leave`() {
    assertEquals(Budget.Range("500", "1500"), budget("500", "1500"))
    assertEquals(Budget.From("500"), budget("500", ""))
    assertEquals(Budget.UpTo("1500"), budget("", "1500"))
    assertEquals(Budget.NotSet, budget("", ""))
    assertEquals(Budget.NotSet, budget("  ", "  "))
  }

  /** Distance needs the helper's own position, which is not tracked. */
  @Test
  fun `location reports the address that was actually given`() {
    assertEquals(TaskLocation.Address("Sector 62, Noida"), taskLocation("Sector 62, Noida", false))
    assertEquals(TaskLocation.PinnedOnMap, taskLocation("", true))
    assertEquals(TaskLocation.NotGiven, taskLocation("", false))
  }
}
