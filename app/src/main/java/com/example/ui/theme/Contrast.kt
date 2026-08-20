package com.example.ui.theme

import kotlin.math.pow

/**
 * WCAG 2.1 contrast maths, kept free of Android so the palette can be checked by a plain JVM test.
 *
 * This exists so that a colour which fails accessibility cannot be added to [Palette] without a
 * test going red — the previous palette shipped orange text at 2.87:1 and grey captions at 2.54:1.
 */
object Contrast {

  /** WCAG AA for body text. */
  const val AA_BODY = 4.5

  /** WCAG AA for large text (18.66px bold or 24px regular) and for meaningful graphics. */
  const val AA_LARGE = 3.0

  /** Relative luminance of an ARGB colour, per WCAG 2.1. Alpha is ignored. */
  fun relativeLuminance(argb: Long): Double {
    val r = channel((argb shr 16 and 0xFF).toInt())
    val g = channel((argb shr 8 and 0xFF).toInt())
    val b = channel((argb and 0xFF).toInt())
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
  }

  /** Contrast ratio between two ARGB colours, from 1.0 (identical) to 21.0 (black on white). */
  fun ratio(foreground: Long, background: Long): Double {
    val a = relativeLuminance(foreground)
    val b = relativeLuminance(background)
    val lighter = maxOf(a, b)
    val darker = minOf(a, b)
    return (lighter + 0.05) / (darker + 0.05)
  }

  private fun channel(value: Int): Double {
    val srgb = value / 255.0
    return if (srgb <= 0.03928) srgb / 12.92 else ((srgb + 0.055) / 1.055).pow(2.4)
  }
}
