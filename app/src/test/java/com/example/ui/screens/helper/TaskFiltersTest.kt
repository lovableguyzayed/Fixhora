package com.example.ui.screens.helper

import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.data.room.declinedByHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskFiltersTest {

  private fun task(
    id: Int,
    status: TaskStatus = TaskStatus.SUBMITTED,
    title: String = "",
    details: String = "",
    location: String = "",
    categoryId: String? = "repairs",
  ) =
    TaskEntity(
      id = id,
      status = status,
      ownerId = "demo",
      categoryId = categoryId,
      locationQuery = location,
      useCurrentLocation = false,
      selectedDistance = 5,
      descriptionTitle = title,
      descriptionDetails = details,
      minBudget = "",
      maxBudget = "",
      photoUris = "",
      createdAt = 0L,
    )

  private val tasks =
    listOf(
      task(1, TaskStatus.SUBMITTED, title = "Leaking kitchen sink", location = "Noida"),
      task(2, TaskStatus.ACCEPTED, title = "Deep clean flat", location = "Bengaluru", categoryId = "cleaning"),
      task(3, TaskStatus.IN_PROGRESS, title = "Paint one wall", location = "Kolkata", categoryId = "painting"),
      task(4, TaskStatus.COMPLETED, title = "Fix ceiling fan", location = "Lucknow"),
      task(5, TaskStatus.REJECTED, title = "Move furniture", location = "Mumbai", categoryId = "moving"),
    )

  /** The bug this replaces: "In Progress" and "Cancelled" fell through to `else -> false`. */
  @Test
  fun `every tab selects its own status`() {
    assertEquals(listOf(1), tasks.filterFor(TaskTab.NEW, "").map { it.id })
    assertEquals(listOf(2), tasks.filterFor(TaskTab.ACCEPTED, "").map { it.id })
    assertEquals(listOf(3), tasks.filterFor(TaskTab.IN_PROGRESS, "").map { it.id })
    assertEquals(listOf(4), tasks.filterFor(TaskTab.COMPLETED, "").map { it.id })
    assertEquals(listOf(5), tasks.filterFor(TaskTab.DECLINED, "").map { it.id })
  }

  @Test
  fun `the all tab keeps everything`() {
    assertEquals(tasks.map { it.id }, tasks.filterFor(TaskTab.ALL, "").map { it.id })
  }

  @Test
  fun `every tab is reachable, so none can be permanently empty`() {
    TaskTab.entries.forEach { tab ->
      assertTrue("${tab.name} matched nothing", tasks.filterFor(tab, "").isNotEmpty())
    }
  }

  /** Category titles come from the caller, in whatever language is on screen. */
  private val englishTitles =
    mapOf("repairs" to "Home Repairs", "cleaning" to "Cleaning", "painting" to "Painting",
          "moving" to "Moving Help")

  /** The search box previously accepted input and filtered nothing. */
  @Test
  fun `search matches title, details, location and category name`() {
    assertEquals(listOf(1), tasks.filterFor(TaskTab.ALL, "sink").map { it.id })
    assertEquals(listOf(2), tasks.filterFor(TaskTab.ALL, "bengaluru").map { it.id })
    assertEquals(listOf(3), tasks.filterFor(TaskTab.ALL, "Painting", englishTitles).map { it.id })
    assertTrue(tasks.filterFor(TaskTab.ALL, "definitely-not-there").isEmpty())
  }

  /**
   * The point of taking the titles as a parameter: a Hindi user types Hindi, and the search has to
   * match what is actually on their screen rather than the English the data happens to carry.
   */
  @Test
  fun `search matches the category name in whatever language was supplied`() {
    val hindiTitles = mapOf("painting" to "पेंटिंग")

    assertEquals(listOf(3), tasks.filterFor(TaskTab.ALL, "पेंटिंग", hindiTitles).map { it.id })
    // The English name is not on screen in that language, so it must not match.
    assertTrue(tasks.filterFor(TaskTab.ALL, "Painting", hindiTitles).isEmpty())
  }

  /** With no titles supplied, search still works on everything else the task carries. */
  @Test
  fun `search without category titles falls back to the task's own fields`() {
    assertEquals(listOf(1), tasks.filterFor(TaskTab.ALL, "sink").map { it.id })
    assertTrue(tasks.filterFor(TaskTab.ALL, "Painting").isEmpty())
  }

  @Test
  fun `search is case and whitespace insensitive`() {
    assertEquals(listOf(1), tasks.filterFor(TaskTab.ALL, "  LEAKING  ").map { it.id })
    assertEquals(tasks.size, tasks.filterFor(TaskTab.ALL, "   ").size)
  }

  @Test
  fun `tab and search apply together`() {
    assertEquals(listOf(1), tasks.filterFor(TaskTab.NEW, "sink").map { it.id })
    assertTrue(tasks.filterFor(TaskTab.COMPLETED, "sink").isEmpty())
  }

  @Test
  fun `a task with no details still matches on its other fields`() {
    val sparse = listOf(task(9, title = "", details = "", location = "Pune", categoryId = null))

    assertTrue(sparse.filterFor(TaskTab.ALL, "pune").isNotEmpty())
    assertFalse(sparse.first().matchesQuery("repairs"))
  }

  @Test
  fun `conversations cover live and finished engagements only`() {
    assertEquals(listOf(2, 3, 4), tasks.conversations().map { it.id })
  }

  @Test
  fun `stats count each status`() {
    val stats = tasks.toHelperStats()

    assertEquals(1, stats.availableNow)
    assertEquals(1, stats.accepted)
    assertEquals(1, stats.inProgress)
    assertEquals(1, stats.completed)
    assertEquals(1, stats.declined)
    assertEquals(2, stats.activeNow)
  }

  @Test
  fun `stats of an empty list are all zero`() {
    val stats = emptyList<TaskEntity>().toHelperStats()

    assertEquals(HelperStats(), stats)
    assertEquals(0, stats.activeNow)
  }

  // ------------------------------------------------------------------ one helper's view

  private val open = task(10, TaskStatus.SUBMITTED, title = "Fix tap")
  private val declinedByMe = task(11, TaskStatus.SUBMITTED, title = "Paint door").declinedByHelper("me")
  private val declinedByOther = task(12, TaskStatus.SUBMITTED, title = "Move sofa").declinedByHelper("other")
  private val mine = task(13, TaskStatus.ACCEPTED, title = "Clean flat").copy(acceptedByHelperId = "me")
  private val someoneElses = task(14, TaskStatus.IN_PROGRESS, title = "Wire lamp").copy(acceptedByHelperId = "other")
  private val legacyAccepted = task(15, TaskStatus.COMPLETED, title = "Old job")
  private val legacyRejected = task(16, TaskStatus.REJECTED, title = "Old decline")
  private val world = listOf(open, declinedByMe, declinedByOther, mine, someoneElses, legacyAccepted, legacyRejected)

  /** The bug: one helper's "Not for me" set the task to REJECTED and hid it from everyone. */
  @Test
  fun `declining hides a job from that helper only`() {
    assertFalse(declinedByMe.isOpenFor("me"))
    assertTrue(declinedByMe.isOpenFor("other"))
    assertTrue(declinedByOther.isOpenFor("me"))
    assertEquals(TaskStatus.SUBMITTED, declinedByMe.status)
  }

  @Test
  fun `the new tab shows only jobs open to this helper`() {
    assertEquals(listOf(10, 12), world.filterFor(TaskTab.NEW, "", helperId = "me").map { it.id })
  }

  @Test
  fun `a job someone else took is not on this helper's lists`() {
    val all = world.filterFor(TaskTab.ALL, "", helperId = "me").map { it.id }
    assertFalse(14 in all)
    assertTrue(13 in all)
  }

  /** Builds before v4 recorded no helper; on one phone such a job is the current helper's. */
  @Test
  fun `a job accepted before helpers were recorded counts as this helper's`() {
    assertTrue(legacyAccepted.isTakenBy("me"))
    assertEquals(listOf(15), world.filterFor(TaskTab.COMPLETED, "", helperId = "me").map { it.id })
  }

  @Test
  fun `the declined tab shows what this helper turned down`() {
    assertEquals(listOf(11, 16), world.filterFor(TaskTab.DECLINED, "", helperId = "me").map { it.id })
  }

  @Test
  fun `a helper's stats are their own`() {
    val stats = world.toHelperStats("me")

    assertEquals(2, stats.availableNow) // 10 and 12
    assertEquals(1, stats.accepted) // 13
    assertEquals(0, stats.inProgress) // 14 is someone else's
    assertEquals(1, stats.completed) // 15
    assertEquals(2, stats.declined) // 11 and 16
  }

  /** A question asked before accepting is a conversation, so it can be answered. */
  @Test
  fun `conversations include open jobs this helper has asked about`() {
    val ids = world.conversationsFor("me", withMessages = setOf(10, 14)).map { it.id }

    assertEquals(listOf(10, 13, 15), ids)
  }
}
