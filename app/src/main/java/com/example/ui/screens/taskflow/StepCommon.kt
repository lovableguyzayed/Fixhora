package com.example.ui.screens.taskflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

/** Title block at the top of every post-a-task step, so all four steps start the same way. */
@Composable
fun StepHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(top = Spacing.sm, bottom = Spacing.xl),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.headlineMedium,
      color = FixTheme.colors.textPrimary,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(Spacing.xs))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = FixTheme.colors.textSecondary,
      textAlign = TextAlign.Center,
    )
  }
}

/**
 * The task flow's text field: theme colours, one shape, a placeholder that never wraps.
 *
 * The steps each styled their own `OutlinedTextField`, with borders at 10–20% opacity that all but
 * vanished on a white card.
 */
@Composable
fun StepTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String? = null,
  placeholder: String? = null,
  leadingIcon: ImageVector? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  prefix: String? = null,
  isError: Boolean = false,
  supportingText: String? = null,
  singleLine: Boolean = true,
  minLines: Int = 1,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
  val colors = FixTheme.colors
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier.fillMaxWidth(),
    label = label?.let { { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
    placeholder =
      placeholder?.let {
        { Text(it, maxLines = if (singleLine) 1 else 3, overflow = TextOverflow.Ellipsis) }
      },
    leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
    trailingIcon = trailingIcon,
    prefix = prefix?.let { { Text(it, color = colors.textPrimary) } },
    isError = isError,
    supportingText = supportingText?.let { { Text(it) } },
    singleLine = singleLine,
    minLines = minLines,
    maxLines = maxLines,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    shape = RoundedCornerShape(Radius.md),
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.border,
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.textSecondary,
        focusedLeadingIconColor = colors.primary,
        unfocusedLeadingIconColor = colors.textSecondary,
        focusedPlaceholderColor = colors.textSecondary,
        unfocusedPlaceholderColor = colors.textSecondary,
        cursorColor = colors.primary,
      ),
  )
}
