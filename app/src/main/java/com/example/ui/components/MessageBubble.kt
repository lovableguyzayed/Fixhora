package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FixTheme

/**
 * One chat message.
 *
 * Lived in `WorkerChatScreen.kt` while only the worker had a chat surface. The customer's side of
 * the same conversation renders identically, so this is shared rather than copied — a second copy
 * would drift, and the two halves of one thread must look like one thread.
 *
 * [isSender] means "written by whoever is reading this screen", not "written by the worker".
 */
@Composable
fun MessageBubble(text: String, isSender: Boolean, time: String) {
    val alignment = if (isSender) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isSender) FixTheme.colors.primary else FixTheme.colors.surface
    val textColor = if (isSender) FixTheme.colors.onPrimary else FixTheme.colors.textPrimary
    val shape =
        if (isSender) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
        } else {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
        }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = alignment) {
        Column(horizontalAlignment = if (isSender) Alignment.End else Alignment.Start) {
            Surface(shape = shape, color = bgColor, modifier = Modifier.widthIn(max = 280.dp)) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // No delivery ticks: nothing tracks whether a message was delivered or read.
            Text(time, fontSize = 11.sp, color = FixTheme.colors.textSecondary)
        }
    }
}
