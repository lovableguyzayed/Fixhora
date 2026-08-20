package com.example.util

/** Length of an Indian subscriber number, without country code. */
const val INDIAN_MOBILE_DIGITS = 10

/**
 * Reduces a typed number to comparable digits, so "+91 98765 43210", "098765 43210" and
 * "9876543210" all resolve to the same account.
 *
 * Anything longer than [INDIAN_MOBILE_DIGITS] keeps its last ten digits, which drops a country
 * code or a leading zero. Shorter input is returned as-is so that validation, not normalisation,
 * decides whether it is acceptable.
 */
fun normalizeMobile(raw: String): String {
  val digits = raw.filter { it.isDigit() }
  return if (digits.length > INDIAN_MOBILE_DIGITS) digits.takeLast(INDIAN_MOBILE_DIGITS) else digits
}

/** Formats a normalised number for display, e.g. "9876543210" -> "+91 98765 43210". */
fun formatIndianMobile(normalized: String): String =
  if (normalized.length == INDIAN_MOBILE_DIGITS) {
    "+91 ${normalized.substring(0, 5)} ${normalized.substring(5)}"
  } else {
    normalized
  }
