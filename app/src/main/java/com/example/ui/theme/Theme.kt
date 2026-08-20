package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.session.ThemePreference

/**
 * The colour set for the active theme.
 *
 * Static rather than dynamic because the whole set changes at once, when the theme flips — there
 * is nothing to gain from tracking reads of individual colours.
 */
val LocalFixColors = staticCompositionLocalOf { LightFixColors }

/** Entry point for design tokens inside a composable: `FixTheme.colors.textPrimary`. */
object FixTheme {
  val colors: FixColors
    @Composable @ReadOnlyComposable get() = LocalFixColors.current

  val spacing: Spacing
    @Composable @ReadOnlyComposable get() = Spacing

  val radius: Radius
    @Composable @ReadOnlyComposable get() = Radius
}

private fun FixColors.toMaterialScheme(isDark: Boolean) =
  if (isDark) {
    darkColorScheme(
      primary = primary,
      onPrimary = onPrimary,
      primaryContainer = primarySurface,
      onPrimaryContainer = textPrimary,
      secondary = accentGraphic,
      onSecondary = onAccent,
      secondaryContainer = accentSurface,
      onSecondaryContainer = accentText,
      background = background,
      onBackground = textPrimary,
      surface = surface,
      onSurface = textPrimary,
      surfaceVariant = surfaceAlt,
      onSurfaceVariant = textSecondary,
      outline = border,
      outlineVariant = border,
      error = danger,
      onError = onPrimary,
      errorContainer = dangerSurface,
      onErrorContainer = danger,
    )
  } else {
    lightColorScheme(
      primary = primary,
      onPrimary = onPrimary,
      primaryContainer = primarySurface,
      onPrimaryContainer = textPrimary,
      secondary = accentGraphic,
      onSecondary = onAccent,
      secondaryContainer = accentSurface,
      onSecondaryContainer = accentText,
      background = background,
      onBackground = textPrimary,
      surface = surface,
      onSurface = textPrimary,
      surfaceVariant = surfaceAlt,
      onSurfaceVariant = textSecondary,
      outline = border,
      outlineVariant = border,
      error = danger,
      onError = onPrimary,
      errorContainer = dangerSurface,
      onErrorContainer = danger,
    )
  }

/**
 * Applies the Fixhora theme.
 *
 * [themePreference] lets the user override the system setting; [ThemePreference.SYSTEM] follows
 * the device. Dynamic colour is deliberately not offered: the blue/orange pairing is the product's
 * identity, and letting the wallpaper recolour it would also throw away the contrast guarantees
 * that `ContrastTest` enforces.
 *
 * `onSurface` is mapped to `textPrimary`, not to a grey. It previously pointed at
 * `SecondaryGrey`, which quietly turned every unstyled body string in the app into low-contrast
 * grey text.
 */
@Composable
fun MyApplicationTheme(
  themePreference: ThemePreference = ThemePreference.SYSTEM,
  content: @Composable () -> Unit,
) {
  val isDark =
    when (themePreference) {
      ThemePreference.SYSTEM -> isSystemInDarkTheme()
      ThemePreference.LIGHT -> false
      ThemePreference.DARK -> true
    }
  val fixColors = if (isDark) DarkFixColors else LightFixColors

  CompositionLocalProvider(LocalFixColors provides fixColors) {
    MaterialTheme(
      colorScheme = fixColors.toMaterialScheme(isDark),
      typography = Typography,
      shapes = FixShapes,
      content = content,
    )
  }
}
