package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = BluePrimary,
    secondary = OrangeSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    outline = DarkOutline
  )

// Every role is set explicitly so no Material baseline (purple) tones leak into
// cards, chips, sheets, switches or app bars.
private val LightColorScheme =
  lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueContainer,
    onPrimaryContainer = BluePrimaryDark,
    inversePrimary = LightBlueBorder,
    secondary = OrangeSecondary,
    onSecondary = Color.White,
    secondaryContainer = BlueContainer,
    onSecondaryContainer = BluePrimary,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = SuccessContainer,
    onTertiaryContainer = SuccessGreen,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = MutedSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = BluePrimary,
    inverseSurface = DarkNavy,
    inverseOnSurface = Color.White,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorRed,
    outline = LightOutline,
    outlineVariant = DividerGrey,
    scrim = Color.Black,
    surfaceBright = Color.White,
    surfaceDim = MutedSurface,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = MutedSurface,
  )

val FixhoraShapes =
  Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Force light theme
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Force custom theme colors
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = FixhoraShapes,
    content = content
  )
}
