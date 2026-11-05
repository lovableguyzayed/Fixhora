package com.example.ui.screens.taskflow

import org.junit.Assert.assertEquals
import org.junit.Test

class DictationTest {

  @Test
  fun `dictation fills an empty description`() {
    assertEquals("Tap is leaking", appendDictation("", "Tap is leaking"))
    assertEquals("Tap is leaking", appendDictation("   ", "Tap is leaking"))
  }

  @Test
  fun `dictation is added after what was typed, with one space`() {
    assertEquals("Kitchen tap. It drips all night", appendDictation("Kitchen tap.  ", "It drips all night"))
  }
}
