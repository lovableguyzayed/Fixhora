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
    /** The yellow that marks "you are here": the active tab, the selected chip, the main pill. */
    val highlight: Long,
    val onHighlight: Long,
    /** Near-black chrome: the floating tab bar and the black pill buttons on coloured cards. */
    val ink: Long,
    val onInk: Long,
  )

  /**
   * Warm off-white page with white cards, near-black text, and a deeper brand blue that still
   * clears 4.5:1 on every surface. The colour comes from the [Bento] cards, not the page.
   */
  val Light =
    Theme(
      primary = 0xFF2148E8,
      primaryPressed = 0xFF1A3BC4,
      onPrimary = 0xFFFFFFFF,
      primarySurface = 0xFFE4E9FD,
      accent = 0xFFFF6A2B,
      accentGraphic = 0xFFD9560F,
      accentText = 0xFFB24A00,
      onAccent = 0xFFFFFFFF,
      accentSurface = 0xFFFFEBDD,
      background = 0xFFF4F2ED,
      surface = 0xFFFFFFFF,
      surfaceAlt = 0xFFEBE8E1,
      border = 0xFFE0DCD3,
      textPrimary = 0xFF141416,
      textSecondary = 0xFF5A5A62,
      textMuted = 0xFF9A9AA2,
      success = 0xFF1E7A34,
      successSurface = 0xFFE3F4E6,
      warning = 0xFFA04700,
      warningSurface = 0xFFFFF0DE,
      danger = 0xFFC0261F,
      dangerSurface = 0xFFFDE8E6,
      info = 0xFF2148E8,
      infoSurface = 0xFFE4E9FD,
      disabled = 0xFFE0DCD3,
      onDisabled = 0xFF8E8E96,
      highlight = 0xFFFFD43B,
      onHighlight = 0xFF141416,
      ink = 0xFF141416,
      onInk = 0xFFFFFFFF,
    )

  /**
   * Charcoal, not navy: the coloured cards are the point of this look and read best on a neutral
   * near-black. The blue is lightened, with dark text on it, because the light-theme blue scores
   * under 3:1 here.
   */
  val Dark =
    Theme(
      primary = 0xFF9DAEFF,
      primaryPressed = 0xFFB9C6FF,
      onPrimary = 0xFF141416,
      primarySurface = 0xFF1E2340,
      accent = 0xFFFF7A3D,
      accentGraphic = 0xFFFF7A3D,
      accentText = 0xFFFF8A4C,
      onAccent = 0xFF141416,
      accentSurface = 0xFF35210F,
      background = 0xFF0F0F11,
      surface = 0xFF1C1C20,
      surfaceAlt = 0xFF27272C,
      border = 0xFF34343A,
      textPrimary = 0xFFFFFFFF,
      textSecondary = 0xFFA8A8B2,
      textMuted = 0xFF6E6E78,
      success = 0xFF6ED17A,
      successSurface = 0xFF15301E,
      warning = 0xFFFFB44D,
      warningSurface = 0xFF35260F,
      danger = 0xFFFF7B72,
      dangerSurface = 0xFF3A1A19,
      info = 0xFF9DAEFF,
      infoSurface = 0xFF1E2340,
      disabled = 0xFF34343A,
      onDisabled = 0xFF77777F,
      highlight = 0xFFFFD43B,
      onHighlight = 0xFF141416,
      ink = 0xFF26262B,
      onInk = 0xFFFFFFFF,
    )

  /**
   * One coloured card: its fill and the text on it.
   *
   * Secondary text on a card is [onFill] at [Bento.SECONDARY_ALPHA]; `ContrastTest` checks the
   * blended result, not just the full-strength text.
   */
  data class Swatch(val fill: Long, val onFill: Long)

  /**
   * The card colours, the same in both themes so a category or a stat keeps its colour when the
   * theme changes. Every fill carries near-black text except [forest], which carries white.
   */
  object Bento {
    private const val INK = 0xFF141416
    val sun = Swatch(fill = 0xFFFFD43B, onFill = INK)
    val lime = Swatch(fill = 0xFFD4E87C, onFill = INK)
    val coral = Swatch(fill = 0xFFFF6F61, onFill = INK)
    val tangerine = Swatch(fill = 0xFFFF6A2B, onFill = INK)
    val periwinkle = Swatch(fill = 0xFF93A5FF, onFill = INK)
    val mint = Swatch(fill = 0xFFA8DDD8, onFill = INK)
    val cream = Swatch(fill = 0xFFF2ECDF, onFill = INK)
    val peach = Swatch(fill = 0xFFFFC4B0, onFill = INK)
    val forest = Swatch(fill = 0xFF1F5C45, onFill = 0xFFFFFFFF)

    val all = listOf(sun, lime, coral, tangerine, periwinkle, mint, cream, peach, forest)

    /** Opacity of secondary text on a card. 0.8 is the most fading the darkest fills allow. */
    const val SECONDARY_ALPHA = 0.8
  }
}
