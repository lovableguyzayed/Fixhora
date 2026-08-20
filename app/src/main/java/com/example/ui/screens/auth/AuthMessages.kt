package com.example.ui.screens.auth

import com.example.util.FieldError
import com.example.util.Validators

/**
 * Turns validation codes into text a person can act on.
 *
 * Every message says what is wrong *and* what to do about it — "Enter a 10-digit mobile number"
 * rather than "Invalid input". Keeping the mapping here, away from the rules themselves, is what
 * will let a Hindi build swap these strings without touching validation.
 */
fun FieldError.message(fieldLabel: String): String =
  when (this) {
    FieldError.REQUIRED -> "$fieldLabel is required"
    FieldError.NAME_TOO_SHORT -> "Enter your full name"
    FieldError.MOBILE_INCOMPLETE -> "Enter a 10-digit mobile number"
    FieldError.MOBILE_INVALID_PREFIX -> "An Indian mobile number starts with 6, 7, 8 or 9"
    FieldError.EMAIL_INVALID -> "Enter a valid email, like name@example.com"
    FieldError.PASSWORD_TOO_SHORT ->
      "Use at least ${Validators.MIN_PASSWORD_LENGTH} characters"
    FieldError.PASSWORD_NEEDS_LETTER_AND_DIGIT -> "Mix at least one letter and one number"
    FieldError.PASSWORD_MISMATCH -> "Both passwords must match"
    FieldError.OTP_INCOMPLETE -> "Enter all ${Validators.OTP_LENGTH} digits"
    FieldError.PIN_CODE_INVALID -> "Enter a valid 6-digit PIN code"
  }

fun FormError.message(): String =
  when (this) {
    FormError.INVALID_CREDENTIALS -> "That mobile number and password do not match. Please try again."
    FormError.MOBILE_ALREADY_REGISTERED ->
      "This number already has an account. Try signing in instead."
    FormError.NO_ACCOUNT_FOR_MOBILE -> "No account found for this number. Create one to continue."
    FormError.OTP_MISMATCH -> "That code is not correct. Check it and try again."
    FormError.UNEXPECTED -> "Something went wrong on this device. Please try again."
  }
