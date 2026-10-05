package com.example.data.room

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskPhotoUrisTest {

  private fun taskWith(photoUris: String) =
    TaskEntity(
      status = TaskStatus.DRAFT,
      ownerId = TaskEntity.GUEST_OWNER_ID,
      categoryId = "repairs",
      locationQuery = "",
      useCurrentLocation = true,
      selectedDistance = 5,
      descriptionTitle = "",
      descriptionDetails = "",
      minBudget = "",
      maxBudget = "",
      photoUris = photoUris,
      createdAt = 0L,
    )

  @Test
  fun `no photos reads back as an empty list`() {
    assertEquals(emptyList<String>(), taskWith("").photoUriList())
  }

  @Test
  fun `photos round-trip through storage`() {
    val uris = listOf("file:///data/user/0/com.example/files/task_photos/a.jpg", "file:///b.jpg")

    assertEquals(uris, taskWith(uris.joinPhotoUris()).photoUriList())
  }

  /**
   * The reason the separator is a newline rather than a comma: a comma is legal inside a URI, and
   * splitting on it silently tore one photo into two unusable fragments.
   */
  @Test
  fun `a comma inside a URI does not split it`() {
    val uris = listOf("content://media/picker/0/item?size=100,200", "file:///b.jpg")

    val stored = uris.joinPhotoUris()

    assertEquals(uris, taskWith(stored).photoUriList())
    assertEquals(2, taskWith(stored).photoUriList().size)
  }

  @Test
  fun `blank entries are dropped in both directions`() {
    assertEquals(listOf("file:///a.jpg"), listOf("file:///a.jpg", "", "  ").joinPhotoUris().let { taskWith(it).photoUriList() })
    assertEquals(emptyList<String>(), taskWith("\n\n").photoUriList())
  }
}
