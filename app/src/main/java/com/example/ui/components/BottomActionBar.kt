package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Spacing

/**
 * Keeps a screen's main action pinned under its scrolling content.
 *
 * The task steps put "Continue" at the end of the scroll, so on any step longer than the screen the
 * user had to scroll to the bottom to find out how to move on.
 */
@Composable
fun BottomActionBar(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Surface(modifier = modifier.fillMaxWidth(), color = FixTheme.colors.background) {
    Column {
      HorizontalDivider(color = FixTheme.colors.border)
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xl, vertical = Spacing.md),
        content = content,
      )
    }
  }
}
