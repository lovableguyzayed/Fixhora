package com.example.ui.screens.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.util.IndiaFormat

/** "Just now", "20 min ago", "3 hr ago", otherwise an IST date like "09/11/2026, 3:05 pm". */
@Composable
fun timeAgo(millis: Long, now: Long = System.currentTimeMillis()): String? {
    if (millis <= 0) return null
    val minutes = ((now - millis) / 60_000).coerceAtLeast(0)
    return when {
        minutes < 1 -> stringResource(R.string.time_just_now)
        minutes < 60 -> stringResource(R.string.time_min_ago, minutes.toInt())
        minutes < 24 * 60 -> stringResource(R.string.time_hr_ago, (minutes / 60).toInt())
        else -> IndiaFormat.chatTime(millis, now)
    }
}

@Composable
fun Badge(text: String, bg: Color, fg: Color) {
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

/** Circle with the first letter of a name (no fake photos). */
@Composable
fun InitialAvatar(name: String, size: Dp = 40.dp, bg: Color = Color(0xFFFFE7D6), fg: Color = Color(0xFFFF6B00)) {
    Box(Modifier.size(size).background(bg, CircleShape), contentAlignment = Alignment.Center) {
        Text(name.trim().take(1).uppercase().ifEmpty { "?" }, color = fg, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.4f).sp)
    }
}

val GreenBg = Color(0xFFE8F5E9); val GreenFg = Color(0xFF2E7D32)
val AmberBg = Color(0xFFFFF3E0); val AmberFg = Color(0xFFB45309)
val BlueBg = Color(0xFFE3F2FD); val GreyBg = Color(0xFFF1F3F5)
val PageBg = Color(0xFFF8F9FA)
