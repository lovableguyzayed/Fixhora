package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

  @Test
  fun `full name must be present and meaningful`() {
    assertEquals(FieldError.REQUIRED, Validators.validateFullName(""))
    assertEquals(FieldError.REQUIRED, Validators.validateFullName("   "))
    assertEquals(FieldError.NAME_TOO_SHORT, Validators.validateFullName("A"))
    assertNull(Validators.validateFullName("Zayed"))
    assertNull(Validators.validateFullName("  Anita Sharma  "))
  }

  /** The sign-in screen previously accepted an empty mobile number and navigated anyway. */
  @Test
  fun `mobile must be a complete Indian number`() {
    assertEquals(FieldError.REQUIRED, Validators.validateMobile(""))
    assertEquals(FieldError.REQUIRED, Validators.validateMobile("abcd"))
    assertEquals(FieldError.MOBILE_INCOMPLETE, Validators.validateMobile("98765"))
    assertEquals(FieldError.MOBILE_INVALID_PREFIX, Validators.validateMobile("5876543210"))
    assertEquals(FieldError.MOBILE_INVALID_PREFIX, Validators.validateMobile("0123456789"))
  }

  @Test
  fun `mobile accepts every common way of typing the same number`() {
    assertNull(Validators.validateMobile("9876543210"))
    assertNull(Validators.validateMobile("+91 98765 43210"))
    assertNull(Validators.validateMobile("09876543210"))
    listOf('6', '7', '8', '9').forEach { prefix ->
      assertNull(Validators.validateMobile("${prefix}123456789"))
    }
  }

  @Test
  fun `email is optional but must be well formed when supplied`() {
    assertNull(Validators.validateEmailOptional(""))
    assertNull(Validators.validateEmailOptional("   "))
    assertNull(Validators.validateEmailOptional("zayed@example.com"))
    assertNull(Validators.validateEmailOptional("zayed+fixhora@example.co.in"))
    assertNull(Validators.validateEmailOptional("a.b@mail.example.com"))

    assertEquals(FieldError.EMAIL_INVALID, Validators.validateEmailOptional("zayed.example.com"))
    assertEquals(FieldError.EMAIL_INVALID, Validators.validateEmailOptional("zayed@example"))
    assertEquals(FieldError.EMAIL_INVALID, Validators.validateEmailOptional("zayed@example."))
    assertEquals(FieldError.EMAIL_INVALID, Validators.validateEmailOptional("za yed@example.com"))
    assertEquals(FieldError.EMAIL_INVALID, Validators.validateEmailOptional("a@b@example.com"))

    assertEquals(FieldError.REQUIRED, Validators.validateEmailRequired(""))
  }

  @Test
  fun `password needs length and a mix of characters`() {
    assertEquals(FieldError.REQUIRED, Validators.validatePassword(""))
    assertEquals(FieldError.PASSWORD_TOO_SHORT, Validators.validatePassword("abc1234"))
    assertEquals(
      FieldError.PASSWORD_NEEDS_LETTER_AND_DIGIT,
      Validators.validatePassword("passwordonly"),
    )
    assertEquals(
      FieldError.PASSWORD_NEEDS_LETTER_AND_DIGIT,
      Validators.validatePassword("12345678"),
    )
    assertNull(Validators.validatePassword("fixhora1"))
    assertNull(Validators.validatePassword("fixhora1!"))
  }

  @Test
  fun `password confirmation must match exactly`() {
    assertEquals(
      FieldError.REQUIRED,
      Validators.validatePasswordConfirmation("fixhora1", ""),
    )
    assertEquals(
      FieldError.PASSWORD_MISMATCH,
      Validators.validatePasswordConfirmation("fixhora1", "fixhora2"),
    )
    assertEquals(
      FieldError.PASSWORD_MISMATCH,
      Validators.validatePasswordConfirmation("fixhora1", "Fixhora1"),
    )
    assertNull(Validators.validatePasswordConfirmation("fixhora1", "fixhora1"))
  }

  /** The OTP screen previously navigated on "Verify" without looking at the code at all. */
  @Test
  fun `otp must be exactly six digits`() {
    assertEquals(FieldError.REQUIRED, Validators.validateOtp(""))
    assertEquals(FieldError.OTP_INCOMPLETE, Validators.validateOtp("12345"))
    assertEquals(FieldError.OTP_INCOMPLETE, Validators.validateOtp("1234567"))
    assertNull(Validators.validateOtp("123456"))
    assertNull(Validators.validateOtp("12 34 56"))
  }

  @Test
  fun `pin code is optional but must be a real six-digit code`() {
    assertNull(Validators.validatePinCodeOptional(""))
    assertNull(Validators.validatePinCodeOptional("201309"))
    assertEquals(FieldError.PIN_CODE_INVALID, Validators.validatePinCodeOptional("20130"))
    assertEquals(FieldError.PIN_CODE_INVALID, Validators.validatePinCodeOptional("012345"))
    assertEquals(FieldError.PIN_CODE_INVALID, Validators.validatePinCodeOptional("20A309"))
  }
}
