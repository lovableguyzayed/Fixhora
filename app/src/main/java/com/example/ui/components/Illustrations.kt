package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BluePrimary

/**
 * A circle sitting at the bottom of the bounds, extended upwards with a rectangle.
 * Clipping artwork with it lets heads "pop out" of the circle while the bottom of the
 * figure follows the circle's curve.
 */
object PopOutCircleShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val diameter = size.width
        val circleTop = size.height - diameter
        val path = Path().apply {
            addOval(Rect(0f, circleTop, diameter, size.height))
            addRect(Rect(0f, 0f, size.width, circleTop + diameter / 2f))
        }
        return Outline.Generic(path)
    }
}

/** Lightweight illustrated street map used wherever a real map SDK is not wired up yet. */
@Composable
fun StylizedMap(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(Color(0xFFEEF2F7))

        // Parks
        val park = Color(0xFFD7F0DF)
        drawRoundRect(park, Offset(w * 0.05f, h * 0.60f), Size(w * 0.30f, h * 0.24f), CornerRadius(18.dp.toPx()))
        drawRoundRect(park, Offset(w * 0.66f, h * 0.06f), Size(w * 0.26f, h * 0.16f), CornerRadius(18.dp.toPx()))

        // Water
        val river = Path().apply {
            moveTo(w * 0.70f, h)
            cubicTo(w * 0.76f, h * 0.82f, w * 0.92f, h * 0.78f, w, h * 0.64f)
            lineTo(w, h)
            close()
        }
        drawPath(river, Color(0xFFCFE2FA))

        // Minor streets
        val minorStroke = 4.dp.toPx()
        for (i in 1..5) {
            val x = w * i / 6f
            drawLine(Color.White, Offset(x, 0f), Offset(x, h), minorStroke)
        }
        for (i in 1..6) {
            val y = h * i / 7f
            drawLine(Color.White, Offset(0f, y), Offset(w, y), minorStroke)
        }

        // Main roads: light outline + white fill
        val outline = Color(0xFFDCE3EC)
        val roads = listOf(
            Offset(0f, h * 0.40f) to Offset(w, h * 0.30f),
            Offset(w * 0.40f, 0f) to Offset(w * 0.55f, h),
            Offset(0f, h * 0.92f) to Offset(w * 0.62f, h * 0.52f),
        )
        roads.forEach { (start, end) ->
            drawLine(outline, start, end, 13.dp.toPx(), cap = StrokeCap.Round)
        }
        roads.forEach { (start, end) ->
            drawLine(Color.White, start, end, 10.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

/** Blue "you are here" dot with a soft pulsing halo. */
@Composable
fun UserLocationDot(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulseScale"
    )
    Box(modifier = modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(BluePrimary.copy(alpha = 0.16f))
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(BluePrimary)
                .border(3.dp, Color.White, CircleShape)
        )
    }
}

/** Width / height of the persona illustrations in drawable-nodpi. */
const val CustomerArtAspect = 488f / 720f
const val WorkerArtAspect = 394f / 720f
