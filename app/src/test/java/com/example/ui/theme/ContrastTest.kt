package com.example.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Enforces WCAG AA across the whole palette, in both themes.
 *
 * This is the guard that stops an unreadable colour from being added. The palette it replaced
 * shipped orange text at 2.86:1, grey captions at 2.54:1, and a dark-mode primary at 2.97:1 —
 * all of which would fail here.
 */
class ContrastTest {

  private fun assertAtLeast(label: String, foreground: Long, background: Long, minimum: Double) {
    val ratio = Contrast.ratio(foreground, background)
    assertTrue(
      "$label: %.2f:1, needs %.1f:1".format(ratio, minimum),
      ratio >= minimum,
    )
  }

  private fun checkTheme(name: String, theme: Palette.Theme) {
    // Body text.
    assertAtLeast("$name textPrimary/background", theme.textPrimary, theme.background, Contrast.AA_BODY)
    assertAtLeast("$name textPrimary/surface", theme.textPrimary, theme.surface, Contrast.AA_BODY)
    assertAtLeast("$name textPrimary/surfaceAlt", theme.textPrimary, theme.surfaceAlt, Contrast.AA_BODY)
    assertAtLeast("$name textSecondary/background", theme.textSecondary, theme.background, Contrast.AA_BODY)
    assertAtLeast("$name textSecondary/surface", theme.textSecondary, theme.surface, Contrast.AA_BODY)
    assertAtLeast("$name textSecondary/surfaceAlt", theme.textSecondary, theme.surfaceAlt, Contrast.AA_BODY)

    // Links and accent text.
    assertAtLeast("$name primary/background", theme.primary, theme.background, Contrast.AA_BODY)
    assertAtLeast("$name primary/surface", theme.primary, theme.surface, Contrast.AA_BODY)
    assertAtLeast("$name primary/surfaceAlt", theme.primary, theme.surfaceAlt, Contrast.AA_BODY)
    assertAtLeast("$name accentText/background", theme.accentText, theme.background, Contrast.AA_BODY)
    assertAtLeast("$name accentText/surface", theme.accentText, theme.surface, Contrast.AA_BODY)

    // Filled buttons.
    assertAtLeast("$name onPrimary/primary", theme.onPrimary, theme.primary, Contrast.AA_BODY)
    assertAtLeast("$name onPrimary/primaryPressed", theme.onPrimary, theme.primaryPressed, Contrast.AA_BODY)
    assertAtLeast("$name onAccent/accentText", theme.onAccent, theme.accentText, Contrast.AA_BODY)

    // Status text, both on its own tinted surface and on a plain one.
    assertAtLeast("$name success/successSurface", theme.success, theme.successSurface, Contrast.AA_BODY)
    assertAtLeast("$name warning/warningSurface", theme.warning, theme.warningSurface, Contrast.AA_BODY)
    assertAtLeast("$name danger/dangerSurface", theme.danger, theme.dangerSurface, Contrast.AA_BODY)
    assertAtLeast("$name info/infoSurface", theme.info, theme.infoSurface, Contrast.AA_BODY)
    assertAtLeast("$name success/surface", theme.success, theme.surface, Contrast.AA_BODY)
    assertAtLeast("$name warning/surface", theme.warning, theme.surface, Contrast.AA_BODY)
    assertAtLeast("$name danger/surface", theme.danger, theme.surface, Contrast.AA_BODY)

    // Meaningful non-text.
    assertAtLeast("$name accentGraphic/background", theme.accentGraphic, theme.background, Contrast.AA_LARGE)
    assertAtLeast("$name accentGraphic/surface", theme.accentGraphic, theme.surface, Contrast.AA_LARGE)

    // The two surfaces have to be told apart, or cards vanish into the page.
    assertTrue(
      "$name surface and surfaceAlt are indistinguishable",
      Contrast.ratio(theme.surface, theme.surfaceAlt) > 1.02,
    )
  }

  @Test fun `light theme meets WCAG AA`() = checkTheme("light", Palette.Light)

  @Test fun `dark theme meets WCAG AA`() = checkTheme("dark", Palette.Dark)

  /** If this drifts, every other assertion here is measuring the wrong thing. */
  @Test
  fun `contrast maths matches the WCAG reference points`() {
    assertEquals(21.0, Contrast.ratio(0xFF000000, 0xFFFFFFFF), 0.01)
    assertEquals(1.0, Contrast.ratio(0xFF808080, 0xFF808080), 0.001)
  }

  /** The specific values this palette replaced, kept as evidence that the change was needed. */
  @Test
  fun `the colours that were replaced really did fail`() {
    assertTrue("old accent text", Contrast.ratio(0xFFFF6B00, 0xFFFFFFFF) < Contrast.AA_BODY)
    assertTrue("old muted caption", Contrast.ratio(0xFF9CA3AF, 0xFFFFFFFF) < Contrast.AA_BODY)
    assertTrue("old dark-mode primary", Contrast.ratio(0xFF0B57FF, 0xFF152033) < Contrast.AA_BODY)
  }
}
