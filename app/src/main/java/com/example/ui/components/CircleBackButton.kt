package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FixTheme

/**
 * The back button used in every top bar: a 40dp tinted circle inside a 48dp touch target, so
 * "back" looks the same whichever flow the user is in.
 */
@Composable
fun CircleBackButton(
  onClick: () -> Unit,
  contentDescription: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  IconButton(onClick = onClick, enabled = enabled, modifier = modifier) {
    Box(
      modifier = Modifier.size(40.dp).clip(CircleShape).background(FixTheme.colors.surfaceAlt),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = contentDescription,
        tint = FixTheme.colors.textPrimary,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}
