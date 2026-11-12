package com.example.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Theme + white surface + readable maximum width. On tablets, unfolded foldables and landscape
 * the content stays centred at phone-like width instead of stretching buttons edge to edge
 * (Android 16 ignores orientation locks on large screens).
 */
@Composable
fun AppFrame(content: @Composable () -> Unit) {
  MyApplicationTheme {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      Box(contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = 640.dp).fillMaxSize()) { content() }
      }
    }
  }
}
