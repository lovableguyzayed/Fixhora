package com.example

import com.example.util.IndiaFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IndiaFormatTest {
    @Test fun cleansPastedNumbers() {
        assertEquals("9876543210", IndiaFormat.cleanMobile("+91 98765-43210"))
        assertEquals("9876543210", IndiaFormat.cleanMobile("098765 43210"))
        assertEquals("9876543210", IndiaFormat.cleanMobile("919876543210"))
        assertEquals("9876543210", IndiaFormat.cleanMobile("00919876543210"))
        assertEquals("9876543210", IndiaFormat.cleanMobile("(98765) 43210"))
        assertEquals("9198765432", IndiaFormat.cleanMobile("9198765432")) // real number starting 91
        assertEquals("98765", IndiaFormat.cleanMobile("0 98765"))
        assertEquals("", IndiaFormat.cleanMobile("abc"))
    }

    @Test fun validatesIndianMobiles() {
        assertTrue(IndiaFormat.isValidMobile("9876543210"))
        assertTrue(IndiaFormat.isValidMobile("6000000000"))
        assertFalse(IndiaFormat.isValidMobile("5876543210"))
        assertFalse(IndiaFormat.isValidMobile("987654321"))
        assertFalse(IndiaFormat.isValidMobile(""))
    }

    @Test fun formatsRupeesWithIndianGrouping() {
        assertEquals("₹500", IndiaFormat.rupees(500))
        assertEquals("₹1,25,000", IndiaFormat.rupees(125000))
        assertEquals("₹10,00,00,000", IndiaFormat.rupees(100000000))
    }

    @Test fun budgetLabels() {
        assertEquals("₹500 – ₹1,500", IndiaFormat.budgetLabel("500", "1500"))
        assertEquals("From ₹500", IndiaFormat.budgetLabel("500", ""))
        assertEquals("Up to ₹1,500", IndiaFormat.budgetLabel("", "1500"))
        assertEquals("₹800", IndiaFormat.budgetLabel("800", "800"))
        assertEquals("Budget negotiable", IndiaFormat.budgetLabel("", ""))
        assertEquals("Budget negotiable", IndiaFormat.budgetLabel("abc", ""))
    }

    @Test fun amountCleaningAndRange() {
        assertEquals("1500", IndiaFormat.cleanAmount("₹1,500.50".substringBefore('.')))
        assertEquals("500", IndiaFormat.cleanAmount("00500"))
        assertEquals("1234567", IndiaFormat.cleanAmount("123456789"))
        assertTrue(IndiaFormat.isBudgetRangeInvalid("2000", "500"))
        assertFalse(IndiaFormat.isBudgetRangeInvalid("500", "2000"))
        assertFalse(IndiaFormat.isBudgetRangeInvalid("", "2000"))
    }

    @Test fun chatTimeIsIst() {
        // 2026-11-09 19:00 UTC = 2026-11-10 00:30 IST (must show the IST day, not the UTC day)
        val t = 1794250800000L
        val sameDayNow = t + 60_000
        assertEquals("12:30 am", IndiaFormat.chatTime(t, sameDayNow))
        assertEquals("10/11/2026, 12:30 am", IndiaFormat.chatTime(t, t + 3 * 86_400_000L))
    }
}
