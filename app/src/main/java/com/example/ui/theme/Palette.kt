package com.example.ui.theme

/**
 * Every colour in the app, as raw ARGB values.
 *
 * These are deliberately plain `Long`s rather than Compose `Color`s so the palette carries no
 * Android dependency and its contrast can be checked by an ordinary JVM test — see
 * `ContrastTest`. `Color.kt` is the only place that wraps them for Compose.
 *
 * Naming is semantic, not visual: screens ask for `textSecondary`, never for "grey", which is what
 * makes a second theme possible at all.
 */
object Palette {

  /**
   * One theme's worth of colour decisions.
   *
   * The brand orange needs three separate roles, because a single value cannot serve all of them.
   * Measured on white: `#FF6B00` reaches only 2.86:1, which fails even the 3:1 required of a
   * meaningful icon, let alone the 4.5:1 required of text. So:
   * - [accent] keeps the brand orange for **decorative** use — the logo, illustrations, large
   *   fills that carry no information on their own. WCAG does not rate these.
   * - [accentGraphic] is the darkened variant for icons and indicators that *do* carry meaning.
   * - [accentText] is darker still, for anything the user has to read.
   *
   * Collapsing these into one value is what put 2.86:1 orange labels on white cards.
   */
  data class Theme(
    val primary: Long,
    val primaryPressed: Long,
    val onPrimary: Long,
    val primarySurface: Long,
    val accent: Long,
    val accentGraphic: Long,
    val accentText: Long,
    val onAccent: Long,
    val accentSurface: Long,
    val background: Long,
    val surface: Long,
    val surfaceAlt: Long,
    val border: Long,
    val textPrimary: Long,
    val textSecondary: Long,
    /** Decorative and disabled use only — not rated for body text. */
    val textMuted: Long,
    val success: Long,
    val successSurface: Long,
    val warning: Long,
    val warningSurface: Long,
    val danger: Long,
    val dangerSurface: Long,
    val info: Long,
    val infoSurface: Long,
    val disabled: Long,
    val onDisabled: Long,
  )

  val Light =
    Theme(
      primary = 0xFF0B57FF,
      primaryPressed = 0xFF0A4AD9,
      onPrimary = 0xFFFFFFFF,
      primarySurface = 0xFFE8F0FE,
      accent = 0xFFFF6B00,
      accentGraphic = 0xFFE85D00,
      accentText = 0xFFC25100,
      onAccent = 0xFFFFFFFF,
      accentSurface = 0xFFFFF3E0,
      background = 0xFFFFFFFF,
      surface = 0xFFFFFFFF,
      surfaceAlt = 0xFFF8F9FA,
      border = 0xFFE5E7EB,
      textPrimary = 0xFF0B1F44,
      textSecondary = 0xFF6B7280,
      textMuted = 0xFF9CA3AF,
      success = 0xFF2E7D32,
      successSurface = 0xFFE8F5E9,
      // Darkened from the familiar #EF6C00 / #D32F2F: both fall just under AA once they sit on
      // their own tinted surface (4.45:1 and 4.36:1), which is exactly where they are used.
      warning = 0xFFA84B00,
      warningSurface = 0xFFFFF3E0,
      danger = 0xFFC62828,
      dangerSurface = 0xFFFFEBEE,
      info = 0xFF0B57FF,
      infoSurface = 0xFFE3F2FD,
      disabled = 0xFFE5E7EB,
      onDisabled = 0xFF9CA3AF,
    )

  /**
   * Dark is not the light palette inverted: the brand blue `#0B57FF` scores 2.98:1 on the dark
   * surface, so dark mode uses a lighter blue with dark text on top of it.
   */
  val Dark =
    Theme(
      primary = 0xFF7EA6FF,
      primaryPressed = 0xFFA3BEFF,
      onPrimary = 0xFF0B1F44,
      primarySurface = 0xFF17243D,
      accent = 0xFFFF8A33,
      accentGraphic = 0xFFFF8A33,
      accentText = 0xFFFF8A33,
      onAccent = 0xFF0B1F44,
      accentSurface = 0xFF3A2A12,
      background = 0xFF0A1020,
      surface = 0xFF152033,
      surfaceAlt = 0xFF1C2942,
      border = 0xFF2D3C5C,
      textPrimary = 0xFFFFFFFF,
      textSecondary = 0xFFA9B4CC,
      textMuted = 0xFF7C8AA6,
      success = 0xFF6ED17A,
      successSurface = 0xFF163726,
      warning = 0xFFFFB44D,
      warningSurface = 0xFF3A2A12,
      danger = 0xFFFF6B6B,
      dangerSurface = 0xFF3B1A1D,
      info = 0xFF7EA6FF,
      infoSurface = 0xFF17243D,
      disabled = 0xFF2D3C5C,
      onDisabled = 0xFF6B7A96,
    )
}
