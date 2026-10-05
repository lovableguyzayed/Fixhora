package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import com.example.R
import com.example.ui.theme.FixTheme

/**
 * The search box at the top of a list.
 *
 * Each list screen built its own, and the placeholder was allowed to wrap: "Search by title,
 * address or category" broke onto two lines and made that field taller than the others.
 */
@Composable
fun SearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
) {
  val colors = FixTheme.colors
  val keyboard = LocalSoftwareKeyboardController.current
  OutlinedTextField(
    value = query,
    onValueChange = onQueryChange,
    modifier = modifier.fillMaxWidth(),
    placeholder = { Text(placeholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
    trailingIcon =
      if (query.isNotEmpty()) {
        {
          IconButton(onClick = { onQueryChange("") }) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_clear_search))
          }
        }
      } else null,
    singleLine = true,
    // Results filter as you type, so the keyboard's search key only needs to get out of the way.
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
    shape = CircleShape,
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.border,
        focusedLeadingIconColor = colors.primary,
        unfocusedLeadingIconColor = colors.textSecondary,
        focusedTrailingIconColor = colors.textSecondary,
        unfocusedTrailingIconColor = colors.textSecondary,
        focusedPlaceholderColor = colors.textSecondary,
        unfocusedPlaceholderColor = colors.textSecondary,
        cursorColor = colors.primary,
      ),
  )
}
