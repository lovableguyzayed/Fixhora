package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Spacing

/**
 * The message box at the foot of a conversation: a rounded field and a round yellow send button.
 *
 * The helper's and the customer's chats each had their own, with different shapes and fills and a
 * bare grey arrow for send that was easy to miss. The send button greys out while there is
 * nothing to send, and [onSend] only ever receives non-blank text.
 */
@Composable
fun ChatInputBar(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  onSend: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = FixTheme.colors
  val canSend = value.isNotBlank()
  Row(
    modifier = modifier.fillMaxWidth().background(colors.background).padding(horizontal = Spacing.md, vertical = Spacing.sm),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier.weight(1f),
      placeholder = { Text(placeholder, maxLines = 1) },
      shape = RoundedCornerShape(28.dp),
      maxLines = 4,
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedTextColor = colors.textPrimary,
          unfocusedTextColor = colors.textPrimary,
          focusedContainerColor = colors.surface,
          unfocusedContainerColor = colors.surface,
          focusedBorderColor = colors.primary,
          unfocusedBorderColor = colors.border,
          focusedPlaceholderColor = colors.textSecondary,
          unfocusedPlaceholderColor = colors.textSecondary,
          cursorColor = colors.primary,
        ),
    )
    Spacer(Modifier.width(Spacing.sm))
    // Attach and voice-note buttons are deliberately absent: neither was ever implemented.
    Box(
      modifier =
        Modifier.size(52.dp)
          .clip(CircleShape)
          .background(if (canSend) colors.highlight else colors.disabled)
          .clickable(enabled = canSend, role = Role.Button) { onSend(value) },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        Icons.AutoMirrored.Filled.Send,
        contentDescription = stringResource(R.string.cd_send_message),
        tint = if (canSend) colors.onHighlight else colors.onDisabled,
        modifier = Modifier.size(22.dp),
      )
    }
  }
}
