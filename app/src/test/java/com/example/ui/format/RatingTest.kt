package com.example.ui.format

import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** A rating is the one number on a helper's card; it must only ever come from real ratings. */
class RatingTest {

  @Test
  fun `no ratings means no average, not zero stars`() {
    assertNull(ratingSummaryOf(emptyList()))
  }

  @Test
  fun `the average and the count come from the ratings given`() {
    assertEquals(RatingSummary(average = 4.0, count = 3), ratingSummaryOf(listOf(5, 4, 3)))
  }

  @Test
  fun `a corrupt rating outside one to five is ignored`() {
    assertEquals(RatingSummary(average = 5.0, count = 1), ratingSummaryOf(listOf(0, 5, 9, -1)))
    assertNull(ratingSummaryOf(listOf(0, 6)))
  }

  @Test
  fun `the average is shown to one decimal`() {
    assertEquals("4.7", formatRatingAverage(4.666))
    assertEquals("4.0", formatRatingAverage(4.0))
    assertEquals("5.0", formatRatingAverage(4.96))
    assertEquals("2.3", formatRatingAverage(2.25))
  }

  private fun task(status: TaskStatus, confirmedAt: Long? = null) =
    TaskEntity(
      status = status,
      ownerId = "c",
      categoryId = "repairs",
      locationQuery = "",
      useCurrentLocation = false,
      selectedDistance = 5,
      descriptionTitle = "t",
      descriptionDetails = "",
      minBudget = "",
      maxBudget = "",
      photoUris = "",
      confirmedAt = confirmedAt,
    )

  @Test
  fun `only a finished job the customer has not confirmed waits for them`() {
    assertTrue(awaitsCustomerConfirmation(task(TaskStatus.COMPLETED)))
    assertFalse(awaitsCustomerConfirmation(task(TaskStatus.COMPLETED, confirmedAt = 1L)))
    assertFalse(awaitsCustomerConfirmation(task(TaskStatus.IN_PROGRESS)))
    assertFalse(awaitsCustomerConfirmation(task(TaskStatus.ACCEPTED)))
  }
}
