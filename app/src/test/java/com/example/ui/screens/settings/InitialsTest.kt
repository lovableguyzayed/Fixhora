package com.example.ui.screens.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialsTest {

  @Test
  fun `first and last words give the initials`() {
    assertEquals("RV", initialsOf("Rahul Verma"))
    assertEquals("RV", initialsOf("Rahul Kumar Verma"))
  }

  @Test
  fun `a single name gives one initial and spacing does not matter`() {
    assertEquals("A", initialsOf("  asha "))
    assertEquals("PS", initialsOf("priya   sharma"))
  }

  @Test
  fun `a blank name gives none`() {
    assertEquals("", initialsOf("   "))
  }

  @Test
  fun `names in Devanagari keep their first letter`() {
    assertEquals("रव", initialsOf("राहुल वर्मा"))
  }
}
