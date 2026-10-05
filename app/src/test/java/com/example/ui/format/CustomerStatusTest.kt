package com.example.ui.format

import com.example.data.room.TaskStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the customer is allowed to do at each stage.
 *
 * The labels moved to resources when the app gained Hindi; what remains here is the rule, which is
 * the part that must not vary by language.
 */
class CustomerStatusTest {

  /**
   * REJECTED means one helper declined, not that the task is dead — it is still open to everyone
   * else, so the customer may still call it off.
   */
  @Test
  fun `a task can be cancelled only while nobody is working on it`() {
    assertTrue(isCancellableByCustomer(TaskStatus.SUBMITTED))
    assertTrue(isCancellableByCustomer(TaskStatus.REJECTED))

    assertFalse(isCancellableByCustomer(TaskStatus.ACCEPTED))
    assertFalse(isCancellableByCustomer(TaskStatus.IN_PROGRESS))
    assertFalse(isCancellableByCustomer(TaskStatus.COMPLETED))
    assertFalse(isCancellableByCustomer(TaskStatus.CANCELLED))
    assertFalse(isCancellableByCustomer(TaskStatus.DRAFT))
  }

  @Test
  fun `a helper exists only from accepted onwards`() {
    assertTrue(hasAssignedHelper(TaskStatus.ACCEPTED))
    assertTrue(hasAssignedHelper(TaskStatus.IN_PROGRESS))
    assertTrue(hasAssignedHelper(TaskStatus.COMPLETED))

    assertFalse(hasAssignedHelper(TaskStatus.SUBMITTED))
    assertFalse(hasAssignedHelper(TaskStatus.REJECTED))
    assertFalse(hasAssignedHelper(TaskStatus.CANCELLED))
    assertFalse(hasAssignedHelper(TaskStatus.DRAFT))
  }

  /**
   * Cancelling a task someone is already working on would leave them building something the
   * customer thinks is dead, with no channel to tell them. The two must never both be offered.
   */
  @Test
  fun `no status offers cancel and chat at the same time`() {
    TaskStatus.entries.forEach { status ->
      assertFalse(
        "${status.name} offers both cancel and chat",
        isCancellableByCustomer(status) && hasAssignedHelper(status),
      )
    }
  }
}
