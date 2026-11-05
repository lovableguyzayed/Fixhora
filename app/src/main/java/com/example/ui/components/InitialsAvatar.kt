package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.format.initialsOf
import com.example.ui.theme.FixTheme

/**
 * A person's initials on a black disc, or a plain person icon when there is no name. Never a stock
 * photo: the app has no pictures of anyone, and a face would suggest it does.
 */
@Composable
fun InitialsAvatar(name: String?, modifier: Modifier = Modifier, size: Dp = 44.dp) {
  val colors = FixTheme.colors
  Box(
    modifier = modifier.size(size).clip(CircleShape).background(colors.ink),
    contentAlignment = Alignment.Center,
  ) {
    val initials = name?.let(::initialsOf)
    if (initials.isNullOrEmpty()) {
      Icon(Icons.Default.Person, contentDescription = null, tint = colors.onInk, modifier = Modifier.size(size / 2))
    } else {
      Text(
        initials,
        style = if (size >= 56.dp) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
        color = colors.onInk,
        fontWeight = FontWeight.ExtraBold,
      )
    }
  }
}
