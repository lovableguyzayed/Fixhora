package com.example.data.room

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SkillIdsTest {

  private fun user(skills: String?) =
    UserEntity(
      id = "u",
      fullName = "U",
      mobile = "9000000000",
      email = null,
      passwordHash = "h",
      passwordSalt = "s",
      gender = null,
      dateOfBirth = null,
      city = "",
      state = "",
      pinCode = "",
      preferredLanguage = "en",
      createdAt = 0L,
      skills = skills,
    )

  @Test
  fun `skills round-trip through storage`() {
    val stored = listOf("repairs", "painting").joinSkillIds()
    assertEquals(listOf("repairs", "painting"), user(stored).skillIds())
  }

  @Test
  fun `no skills is stored as null and read as empty`() {
    assertNull(emptyList<String>().joinSkillIds())
    assertNull(listOf(" ", "").joinSkillIds())
    assertEquals(emptyList<String>(), user(null).skillIds())
  }

  @Test
  fun `duplicates are dropped`() {
    assertEquals("repairs", listOf("repairs", "repairs").joinSkillIds())
  }
}
