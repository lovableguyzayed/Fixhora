package com.example.ui.screens.helper

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkerFormattingTest {

  private val now = 1_700_000_000_000L
  private val minute = 60_000L
  private val hour = 60 * minute
  private val day = 24 * hour

  /** Every job card used to read "10 mins ago" whatever its age. */
  @Test
  fun `relative time describes the real age`() {
    assertEquals("Just now", relativeTimeLabel(now, now))
    assertEquals("Just now", relativeTimeLabel(now - 30_000, now))
    assertEquals("1 min ago", relativeTimeLabel(now - minute, now))
    assertEquals("5 mins ago", relativeTimeLabel(now - 5 * minute, now))
    assertEquals("1 hour ago", relativeTimeLabel(now - hour, now))
    assertEquals("5 hours ago", relativeTimeLabel(now - 5 * hour, now))
    assertEquals("Yesterday", relativeTimeLabel(now - day, now))
    assertEquals("3 days ago", relativeTimeLabel(now - 3 * day, now))
    assertEquals("Over a month ago", relativeTimeLabel(now - 60 * day, now))
  }

  /** Clock skew must not produce "in -3 minutes". */
  @Test
  fun `a future timestamp reads as just now`() {
    assertEquals("Just now", relativeTimeLabel(now + 5 * minute, now))
  }

  /** Was "Customer Name" plus a verified tick and a 4.8 rating, none of it backed by data. */
  @Test
  fun `poster label reports only what is known`() {
    assertEquals("Anita Sharma", posterLabel("u1", mapOf("u1" to "Anita Sharma")))
    assertEquals("Demo request", posterLabel("demo", emptyMap()))
    assertEquals("Guest request", posterLabel("guest", emptyMap()))
    assertEquals("Customer", posterLabel("unknown-id", mapOf("u1" to "Anita")))
    assertEquals("Customer", posterLabel("u1", mapOf("u1" to "")))
  }

  @Test
  fun `budget label covers every combination the customer can leave`() {
    assertEquals("₹500 – ₹1500", budgetLabel("500", "1500"))
    assertEquals("From ₹500", budgetLabel("500", ""))
    assertEquals("Up to ₹1500", budgetLabel("", "1500"))
    assertEquals("No budget set", budgetLabel("", ""))
    assertEquals("No budget set", budgetLabel("  ", "  "))
  }

  /** Was a blanket "2.5 km away"; distance needs the helper's own position, which is not tracked. */
  @Test
  fun `location label shows the address that was actually given`() {
    assertEquals("Sector 62, Noida", locationLabel("Sector 62, Noida", false))
    assertEquals("Location pinned on map", locationLabel("", true))
    assertEquals("No location given", locationLabel("", false))
  }
}
