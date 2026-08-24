package com.example.ui.format

import com.example.data.room.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The customer's view of a task's state.
 *
 * These labels are the only thing telling someone whether anyone has picked their job up, so a
 * missing or duplicated one is a real failure, not a cosmetic one.
 */
class CustomerStatusTest {

  @Test
  fun `every status has a label and a detail, and neither leaks the enum name`() {
    TaskStatus.entries.forEach { status ->
      val label = customerStatusLabel(status)
      val detail = customerStatusDetail(status)

      assertTrue("${status.name} has a blank label", label.isNotBlank())
      assertTrue("${status.name} has a blank detail", detail.isNotBlank())
      assertFalse("${status.name} leaks the enum name", label == status.name)
      assertFalse("${status.name} leaks an underscore", label.contains("_"))
    }
  }

  /** Two states reading the same would make the list unreadable. */
  @Test
  fun `labels are distinct`() {
    val labels = TaskStatus.entries.map { customerStatusLabel(it) }

    assertEquals(labels.size, labels.toSet().size)
  }

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
