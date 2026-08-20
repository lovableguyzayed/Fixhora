package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumbersTest {

  @Test
  fun `the same number typed different ways normalises identically`() {
    val expected = "9876543210"

    assertEquals(expected, normalizeMobile("9876543210"))
    assertEquals(expected, normalizeMobile("+91 98765 43210"))
    assertEquals(expected, normalizeMobile("+919876543210"))
    assertEquals(expected, normalizeMobile("09876543210"))
    assertEquals(expected, normalizeMobile("098765-43210"))
    assertEquals(expected, normalizeMobile("(+91) 98765 43210"))
  }

  @Test
  fun `short input is left alone so validation can reject it`() {
    assertEquals("", normalizeMobile(""))
    assertEquals("", normalizeMobile("abc"))
    assertEquals("98765", normalizeMobile("98765"))
  }

  @Test
  fun `display formatting only applies to complete numbers`() {
    assertEquals("+91 98765 43210", formatIndianMobile("9876543210"))
    assertEquals("98765", formatIndianMobile("98765"))
    assertEquals("", formatIndianMobile(""))
  }
}
