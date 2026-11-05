package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.R
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Spacing

/**
 * The app's top bar: round back button, then a large bold title on the left, on the page colour.
 *
 * Every screen used to configure its own — centred on one, white on another, a plain arrow on a
 * third, titles anywhere from 16sp SemiBold to 22sp Bold. [subtitle] is a short line under the
 * title, for context such as a step count.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixTopBar(
  title: String,
  onBack: (() -> Unit)?,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  backDescription: String = stringResource(R.string.cd_back),
  actions: @Composable RowScope.() -> Unit = {},
) {
  val colors = FixTheme.colors
  TopAppBar(
    modifier = modifier,
    title = {
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = colors.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    navigationIcon = {
      if (onBack != null) {
        CircleBackButton(
          onClick = onBack,
          contentDescription = backDescription,
          modifier = Modifier.padding(start = Spacing.xs),
        )
      }
    },
    actions = actions,
    colors =
      TopAppBarDefaults.topAppBarColors(
        containerColor = colors.background,
        scrolledContainerColor = colors.background,
      ),
  )
}

/** The gear that opens Profile & settings, for the end of a [FixTopBar]. */
@Composable
fun SettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  CircleIconButton(
    icon = Icons.Default.Settings,
    contentDescription = stringResource(R.string.settings_title),
    onClick = onClick,
    container = FixTheme.colors.surfaceAlt,
    content = FixTheme.colors.textPrimary,
    modifier = modifier.padding(end = Spacing.lg),
  )
}
