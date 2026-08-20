package com.example.data.room

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskStatusTest {

  /**
   * These strings are already sitting in installed databases. Changing one silently reclassifies
   * every existing row, so the mapping is pinned here rather than derived from the enum name.
   */
  @Test
  fun `storage values are stable`() {
    assertEquals("draft", TaskStatus.DRAFT.storageValue)
    assertEquals("submitted", TaskStatus.SUBMITTED.storageValue)
    assertEquals("accepted", TaskStatus.ACCEPTED.storageValue)
    assertEquals("in_progress", TaskStatus.IN_PROGRESS.storageValue)
    assertEquals("completed", TaskStatus.COMPLETED.storageValue)
    assertEquals("cancelled", TaskStatus.CANCELLED.storageValue)
    assertEquals("rejected", TaskStatus.REJECTED.storageValue)
  }

  @Test
  fun `every value round-trips through storage`() {
    TaskStatus.entries.forEach { status ->
      assertEquals(status, TaskStatus.fromStorage(status.storageValue))
    }
  }

  @Test
  fun `stored values are read leniently`() {
    assertEquals(TaskStatus.ACCEPTED, TaskStatus.fromStorage("  accepted "))
    assertEquals(TaskStatus.COMPLETED, TaskStatus.fromStorage("Completed"))
  }

  @Test
  fun `unrecognised values degrade to cancelled rather than throwing`() {
    assertEquals(TaskStatus.CANCELLED, TaskStatus.fromStorage("some_old_value"))
    assertEquals(TaskStatus.CANCELLED, TaskStatus.fromStorage(null))
    assertEquals(TaskStatus.CANCELLED, TaskStatus.fromStorage(""))
  }

  @Test
  fun `only submitted tasks are offered to helpers`() {
    assertEquals(listOf(TaskStatus.SUBMITTED), TaskStatus.entries.filter { it.isOpenForHelpers })
  }

  @Test
  fun `accepted and in-progress count as active engagements`() {
    assertEquals(
      listOf(TaskStatus.ACCEPTED, TaskStatus.IN_PROGRESS),
      TaskStatus.entries.filter { it.isActiveEngagement },
    )
  }
}
