package com.example.data.session

import com.example.data.room.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPreferencesTest {

  /** These strings are the ones already used by the role-selection screen's callback. */
  @Test
  fun `role storage values match the existing route arguments`() {
    assertEquals("user", UserRole.CUSTOMER.storageValue)
    assertEquals("helper", UserRole.HELPER.storageValue)
    assertEquals(UserRole.CUSTOMER, UserRole.fromStorage("user"))
    assertEquals(UserRole.HELPER, UserRole.fromStorage("helper"))
  }

  @Test
  fun `an absent or unknown role stays null rather than guessing a side of the marketplace`() {
    assertNull(UserRole.fromStorage(null))
    assertNull(UserRole.fromStorage(""))
    assertNull(UserRole.fromStorage("admin"))
  }

  @Test
  fun `theme and language fall back to safe defaults`() {
    assertEquals(ThemePreference.SYSTEM, ThemePreference.fromStorage(null))
    assertEquals(ThemePreference.SYSTEM, ThemePreference.fromStorage("neon"))
    assertEquals(ThemePreference.DARK, ThemePreference.fromStorage("dark"))

    assertEquals(AppLanguage.ENGLISH, AppLanguage.fromStorage(null))
    assertEquals(AppLanguage.ENGLISH, AppLanguage.fromStorage("fr"))
    assertEquals(AppLanguage.HINDI, AppLanguage.fromStorage("hi"))
  }

  @Test
  fun `a guest session is a supported state, not a signed-in one`() {
    val guest = Session()

    assertFalse(guest.isSignedIn)
    assertEquals(TaskEntity.GUEST_OWNER_ID, guest.ownerId(TaskEntity.GUEST_OWNER_ID))
  }

  @Test
  fun `a signed-in session owns its tasks`() {
    val session = Session(userId = "user-1", activeRole = UserRole.CUSTOMER)

    assertTrue(session.isSignedIn)
    assertEquals("user-1", session.ownerId(TaskEntity.GUEST_OWNER_ID))
  }
}
