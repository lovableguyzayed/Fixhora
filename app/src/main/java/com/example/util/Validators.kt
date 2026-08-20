package com.example.util

/**
 * Why a field was rejected.
 *
 * Validation returns a code rather than a sentence so that the rules stay free of Android and of
 * any particular language: the UI maps these to text, which is what will make a Hindi build
 * possible without touching this file.
 */
enum class FieldError {
  REQUIRED,
  NAME_TOO_SHORT,
  MOBILE_INCOMPLETE,
  MOBILE_INVALID_PREFIX,
  EMAIL_INVALID,
  PASSWORD_TOO_SHORT,
  PASSWORD_NEEDS_LETTER_AND_DIGIT,
  PASSWORD_MISMATCH,
  OTP_INCOMPLETE,
  PIN_CODE_INVALID,
}

/**
 * Field-level rules shared by every auth screen.
 *
 * Each function returns null when the value is acceptable, or the [FieldError] explaining why it
 * is not. They are pure so they can be tested without an emulator.
 */
object Validators {

  const val MIN_PASSWORD_LENGTH = 8
  const val OTP_LENGTH = 6
  const val PIN_CODE_LENGTH = 6

  /** Indian mobile numbers start with 6, 7, 8 or 9 once the country code is stripped. */
  private val VALID_MOBILE_PREFIXES = setOf('6', '7', '8', '9')

  private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

  fun validateFullName(raw: String): FieldError? {
    val trimmed = raw.trim()
    return when {
      trimmed.isEmpty() -> FieldError.REQUIRED
      trimmed.length < 2 -> FieldError.NAME_TOO_SHORT
      else -> null
    }
  }

  fun validateMobile(raw: String): FieldError? {
    val normalized = normalizeMobile(raw)
    return when {
      normalized.isEmpty() -> FieldError.REQUIRED
      normalized.length < INDIAN_MOBILE_DIGITS -> FieldError.MOBILE_INCOMPLETE
      normalized[0] !in VALID_MOBILE_PREFIXES -> FieldError.MOBILE_INVALID_PREFIX
      else -> null
    }
  }

  /** Email is optional at sign-up, so blank is acceptable; malformed is not. */
  fun validateEmailOptional(raw: String): FieldError? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    return if (EMAIL_REGEX.matches(trimmed)) null else FieldError.EMAIL_INVALID
  }

  fun validateEmailRequired(raw: String): FieldError? {
    if (raw.trim().isEmpty()) return FieldError.REQUIRED
    return validateEmailOptional(raw)
  }

  /**
   * Long enough to resist casual guessing, and mixed enough that "password" and "12345678" are
   * both rejected. Deliberately no symbol requirement: it pushes people towards writing passwords
   * down without materially improving them.
   */
  fun validatePassword(raw: String): FieldError? =
    when {
      raw.isEmpty() -> FieldError.REQUIRED
      raw.length < MIN_PASSWORD_LENGTH -> FieldError.PASSWORD_TOO_SHORT
      !(raw.any { it.isLetter() } && raw.any { it.isDigit() }) ->
        FieldError.PASSWORD_NEEDS_LETTER_AND_DIGIT
      else -> null
    }

  fun validatePasswordConfirmation(password: String, confirmation: String): FieldError? =
    when {
      confirmation.isEmpty() -> FieldError.REQUIRED
      confirmation != password -> FieldError.PASSWORD_MISMATCH
      else -> null
    }

  fun validateOtp(raw: String): FieldError? {
    val digits = raw.filter { it.isDigit() }
    return when {
      digits.isEmpty() -> FieldError.REQUIRED
      digits.length != OTP_LENGTH -> FieldError.OTP_INCOMPLETE
      else -> null
    }
  }

  /** Optional during profile setup; when given it must be a real six-digit PIN. */
  fun validatePinCodeOptional(raw: String): FieldError? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    val digits = trimmed.filter { it.isDigit() }
    return if (digits.length == PIN_CODE_LENGTH && digits == trimmed && digits[0] != '0') {
      null
    } else {
      FieldError.PIN_CODE_INVALID
    }
  }
}
