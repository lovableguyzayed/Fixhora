package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * The full type scale.
 *
 * Only `bodyLarge` was defined before, with the rest commented out, so every screen hardcoded its
 * own `fontSize` and the same heading appeared at 24sp on one screen and 28sp on another. Line
 * heights are set explicitly because Devanagari sits taller than Latin and clips at Material's
 * defaults.
 *
 * Plus Jakarta Sans (SIL Open Font License, see `licenses/`), bundled at five weights — about
 * 150 KB, Latin only. Hindi text and the rupee sign are not in it and fall back per character to
 * the system font, which ships a Devanagari face on every Android device.
 *
 * Nothing is smaller than 12sp, and the small sizes most screens use for captions and tags were
 * raised a step (13 to 14, 12 to 13): at 11 and 12sp, budgets, times and status tags were hard to
 * read on the budget phones much of the audience uses, in bright light, and in Devanagari.
 */
private val Sans =
  FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
  )

val Typography =
  Typography(
    displayLarge =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp, lineHeight = 60.sp, letterSpacing = (-1.5).sp),
    displayMedium =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 50.sp, letterSpacing = (-1).sp),
    displaySmall =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineLarge =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp),
    headlineMedium =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.25).sp),
    headlineSmall =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall =
      TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
  )
