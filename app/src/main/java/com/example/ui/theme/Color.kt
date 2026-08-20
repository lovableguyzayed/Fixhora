package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The theme-aware colours, resolved from [Palette].
 *
 * Screens should reach these through `FixTheme.colors`, which returns the set matching the active
 * theme. Reading a colour any other way is what made dark mode unreachable: a `Color.White`
 * written into a screen stays white however the theme changes.
 */
data class FixColors(
  val primary: Color,
  val primaryPressed: Color,
  val onPrimary: Color,
  val primarySurface: Color,
  /** Brand orange. Decorative only — see [accentGraphic] and [accentText]. */
  val accent: Color,
  /** Orange for icons and indicators that carry meaning (>= 3:1). */
  val accentGraphic: Color,
  /** Orange for text (>= 4.5:1). */
  val accentText: Color,
  val onAccent: Color,
  val accentSurface: Color,
  val background: Color,
  val surface: Color,
  val surfaceAlt: Color,
  val border: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  /** Decorative and disabled use only — not rated for body text. */
  val textMuted: Color,
  val success: Color,
  val successSurface: Color,
  val warning: Color,
  val warningSurface: Color,
  val danger: Color,
  val dangerSurface: Color,
  val info: Color,
  val infoSurface: Color,
  val disabled: Color,
  val onDisabled: Color,
)

internal fun Palette.Theme.toFixColors() =
  FixColors(
    primary = Color(primary),
    primaryPressed = Color(primaryPressed),
    onPrimary = Color(onPrimary),
    primarySurface = Color(primarySurface),
    accent = Color(accent),
    accentGraphic = Color(accentGraphic),
    accentText = Color(accentText),
    onAccent = Color(onAccent),
    accentSurface = Color(accentSurface),
    background = Color(background),
    surface = Color(surface),
    surfaceAlt = Color(surfaceAlt),
    border = Color(border),
    textPrimary = Color(textPrimary),
    textSecondary = Color(textSecondary),
    textMuted = Color(textMuted),
    success = Color(success),
    successSurface = Color(successSurface),
    warning = Color(warning),
    warningSurface = Color(warningSurface),
    danger = Color(danger),
    dangerSurface = Color(dangerSurface),
    info = Color(info),
    infoSurface = Color(infoSurface),
    disabled = Color(disabled),
    onDisabled = Color(onDisabled),
  )

val LightFixColors = Palette.Light.toFixColors()
val DarkFixColors = Palette.Dark.toFixColors()

// ---------------------------------------------------------------------------------------------
// Static colours for the few places that are not inside a composition and so cannot read the
// theme — currently only the category list in `CategoriesData.kt`, whose icon tints are
// decorative brand colours rather than semantic roles.
// ---------------------------------------------------------------------------------------------

val BluePrimary = Color(Palette.Light.primary)
val OrangeSecondary = Color(Palette.Light.accent)
val DarkNavy = Color(Palette.Light.textPrimary)
val SecondaryGrey = Color(Palette.Light.textSecondary)
val BorderGrey = Color(Palette.Light.border)
val SuccessGreen = Color(Palette.Light.success)
val LightBlueBorder = Color(0xFFCFE1FF)
val LightOrangeBorder = Color(0xFFFFD9BA)
