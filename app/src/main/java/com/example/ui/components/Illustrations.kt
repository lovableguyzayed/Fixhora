package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FixTheme

/** Width / height of the persona illustrations in `drawable-nodpi`, cropped to the figures. */
const val CustomerArtAspect = 488f / 720f
const val WorkerArtAspect = 394f / 720f

/**
 * A circle sitting at the bottom of the bounds, extended upwards by a rectangle.
 *
 * Clipping a figure with it lets the head rise above the circle while the body is cut by the
 * circle's curve, the "pop-out" treatment the onboarding design uses for both personas.
 */
object PopOutCircleShape : Shape {
  override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
    val diameter = size.width
    val circleTop = size.height - diameter
    val path =
      Path().apply {
        addOval(Rect(0f, circleTop, diameter, size.height))
        addRect(Rect(0f, 0f, size.width, circleTop + diameter / 2f))
      }
    return Outline.Generic(path)
  }
}

/**
 * A drawn street-map backdrop for the map previews.
 *
 * Decorative only, and drawn from theme colours so it follows dark mode. Screens that show it keep
 * their "not an interactive map" label: this replaces a flat tinted box, not that honesty.
 */
@Composable
fun StylizedMap(modifier: Modifier = Modifier) {
  val colors = FixTheme.colors
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    drawRect(colors.surfaceAlt)

    val park = colors.successSurface
    drawRoundRect(park, Offset(w * 0.05f, h * 0.60f), Size(w * 0.30f, h * 0.24f), CornerRadius(18.dp.toPx()))
    drawRoundRect(park, Offset(w * 0.66f, h * 0.06f), Size(w * 0.26f, h * 0.16f), CornerRadius(18.dp.toPx()))

    val river =
      Path().apply {
        moveTo(w * 0.70f, h)
        cubicTo(w * 0.76f, h * 0.82f, w * 0.92f, h * 0.78f, w, h * 0.64f)
        lineTo(w, h)
        close()
      }
    drawPath(river, colors.infoSurface)

    val minor = 4.dp.toPx()
    for (i in 1..5) {
      val x = w * i / 6f
      drawLine(colors.surface, Offset(x, 0f), Offset(x, h), minor)
    }
    for (i in 1..6) {
      val y = h * i / 7f
      drawLine(colors.surface, Offset(0f, y), Offset(w, y), minor)
    }

    val roads =
      listOf(
        Offset(0f, h * 0.40f) to Offset(w, h * 0.30f),
        Offset(w * 0.40f, 0f) to Offset(w * 0.55f, h),
        Offset(0f, h * 0.92f) to Offset(w * 0.62f, h * 0.52f),
      )
    roads.forEach { (start, end) -> drawLine(colors.border, start, end, 13.dp.toPx(), cap = StrokeCap.Round) }
    roads.forEach { (start, end) -> drawLine(colors.surface, start, end, 10.dp.toPx(), cap = StrokeCap.Round) }
  }
}
