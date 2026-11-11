package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Helpers for Indian phone numbers, rupee amounts and IST dates. Pure Kotlin, unit-tested. */
object IndiaFormat {

    private val IN = Locale("en", "IN")
    private val IST: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    /**
     * Cleans what a user typed or pasted ("+91 98765-43210", "098765 43210", "919876543210")
     * down to at most 10 digits of the national number.
     */
    fun cleanMobile(input: String): String {
        var digits = input.filter { it.isDigit() }
        if (digits.length > 10 && digits.startsWith("0091")) digits = digits.drop(4)
        if (digits.length > 10 && digits.startsWith("91")) digits = digits.drop(2)
        if (digits.length > 10 && digits.startsWith("0")) digits = digits.drop(1)
        // A lone leading 0 (trunk prefix) is never part of an Indian mobile number.
        if (digits.startsWith("0")) digits = digits.dropWhile { it == '0' }
        return digits.take(10)
    }

    /** Indian mobiles are 10 digits and start with 6, 7, 8 or 9. */
    fun isValidMobile(cleaned: String): Boolean =
        cleaned.length == 10 && cleaned[0] in '6'..'9' && cleaned.all { it.isDigit() }

    /** "+91 98765 43210" for display. */
    fun displayMobile(cleaned: String): String =
        if (cleaned.length == 10) "+91 ${cleaned.take(5)} ${cleaned.drop(5)}" else "+91 $cleaned"

    /** Keeps only digits for a whole-rupee amount field (max 7 digits = ₹99,99,999). */
    fun cleanAmount(input: String, maxDigits: Int = 7): String =
        input.filter { it.isDigit() }.trimStart('0').take(maxDigits)

    /** 125000 -> "₹1,25,000" (Indian digit grouping). Whole rupees only. */
    fun rupees(amount: Long): String {
        // Grouped by hand (last 3 digits, then pairs) so the result is the same on every phone.
        val digits = kotlin.math.abs(amount).toString()
        val grouped = if (digits.length <= 3) digits else {
            val head = digits.dropLast(3)
            head.reversed().chunked(2).joinToString(",").reversed() + "," + digits.takeLast(3)
        }
        return (if (amount < 0) "-₹" else "₹") + grouped
    }

    /** Formats a stored amount string; returns null when empty or not a number. */
    fun rupeesOrNull(stored: String): String? = stored.trim().toLongOrNull()?.let { rupees(it) }

    /** One budget label used on every screen. */
    fun budgetLabel(min: String, max: String, none: String = "Budget negotiable"): String {
        val lo = rupeesOrNull(min)
        val hi = rupeesOrNull(max)
        return when {
            lo != null && hi != null && lo == hi -> lo
            lo != null && hi != null -> "$lo – $hi"
            lo != null -> "From $lo"
            hi != null -> "Up to $hi"
            else -> none
        }
    }

    /** True when both are filled and min is larger than max. */
    fun isBudgetRangeInvalid(min: String, max: String): Boolean {
        val lo = min.toLongOrNull() ?: return false
        val hi = max.toLongOrNull() ?: return false
        return lo > hi
    }

    /** Chat time in IST: "3:05 pm" today, otherwise "09/11/2026, 3:05 pm". */
    fun chatTime(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val day = SimpleDateFormat("dd/MM/yyyy", IN).apply { timeZone = IST }
        val time = SimpleDateFormat("h:mm a", IN).apply { timeZone = IST }
        val t = time.format(Date(timestampMillis)).lowercase(IN)
        val d = day.format(Date(timestampMillis))
        return if (d == day.format(Date(nowMillis))) t else "$d, $t"
    }
}
