package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

/**
 * The frame every auth and onboarding form shares.
 *
 * Before this, each screen placed its own back arrow (or none), sized its own title, and none of
 * them accounted for the status bar, the navigation bar or the keyboard under edge-to-edge — so
 * the first field could sit under the clock and the submit button under the keyboard.
 */
@Composable
fun AuthScreen(
  onBack: (() -> Unit)?,
  modifier: Modifier = Modifier,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(FixTheme.colors.background)
        .systemBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = Spacing.sm)) {
      if (onBack != null) {
        AuthBackButton(onBack, modifier = Modifier.align(Alignment.CenterStart))
      }
    }
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xl),
      horizontalAlignment = horizontalAlignment,
    ) {
      Spacer(modifier = Modifier.height(Spacing.sm))
      content()
      Spacer(modifier = Modifier.height(Spacing.xl))
    }
  }
}

/** Circular back button, the same on every auth screen. */
@Composable
fun AuthBackButton(onBack: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
  IconButton(onClick = onBack, enabled = enabled, modifier = modifier) {
    Box(
      modifier = Modifier.size(40.dp).clip(CircleShape).background(FixTheme.colors.surfaceAlt),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = stringResource(R.string.cd_go_back),
        tint = FixTheme.colors.textPrimary,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

/**
 * Page title and its supporting line.
 *
 * Uses the type scale rather than a bare `fontSize`: titles set only a size inherited the 24sp line
 * height of body text, so any title that wrapped ("Create Your Account", "Login with Mobile
 * Number") drew its second line over the first.
 */
@Composable
fun AuthHeader(title: String, subtitle: String? = null, textAlign: TextAlign = TextAlign.Start) {
  Text(
    text = title,
    style = MaterialTheme.typography.headlineLarge,
    color = FixTheme.colors.textPrimary,
    textAlign = textAlign,
    modifier = Modifier.fillMaxWidth(),
  )
  if (subtitle != null) {
    Spacer(modifier = Modifier.height(Spacing.sm))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyLarge,
      color = FixTheme.colors.textSecondary,
      textAlign = textAlign,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

/**
 * "Don't have an account? Create New Account". Wraps as a unit when both halves do not fit on one
 * line, instead of breaking the action itself across two lines.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthFooterPrompt(prompt: String, action: String, onClick: () -> Unit, enabled: Boolean = true) {
  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      prompt,
      style = MaterialTheme.typography.bodyMedium,
      color = FixTheme.colors.textSecondary,
      modifier = Modifier.align(Alignment.CenterVertically),
    )
    TextButton(
      onClick = onClick,
      enabled = enabled,
      contentPadding = PaddingValues(horizontal = Spacing.sm),
      modifier = Modifier.height(MinTouchTarget),
    ) {
      Text(action, style = MaterialTheme.typography.titleSmall, color = FixTheme.colors.primary)
    }
  }
}

/**
 * One-time code input: a single hidden text field drawn as [length] boxes.
 *
 * Six separate fields could not move back on backspace from an empty box and fought over focus; a
 * single field gets typing, deleting and pasting right for free.
 */
@Composable
fun OtpCodeInput(
  value: String,
  onValueChange: (String) -> Unit,
  length: Int,
  modifier: Modifier = Modifier,
  isError: Boolean = false,
  enabled: Boolean = true,
  onDone: () -> Unit = {},
  focusRequester: FocusRequester = remember { FocusRequester() },
) {
  val colors = FixTheme.colors
  var focused by remember { mutableStateOf(false) }
  BasicTextField(
    value = TextFieldValue(value, selection = TextRange(value.length)),
    onValueChange = { onValueChange(it.text.filter { c -> c.isDigit() }.take(length)) },
    modifier = modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged { focused = it.isFocused },
    enabled = enabled,
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(onDone = { onDone() }),
    decorationBox = {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        repeat(length) { index ->
          val char = value.getOrNull(index)?.toString().orEmpty()
          val active = focused && (index == value.length || (index == length - 1 && value.length == length))
          Box(
            modifier =
              Modifier.weight(1f)
                .height(56.dp)
                .border(
                  width = if (active) 2.dp else 1.dp,
                  color =
                    when {
                      isError -> colors.danger
                      active -> colors.primary
                      char.isNotEmpty() -> colors.textSecondary
                      else -> colors.border
                    },
                  shape = RoundedCornerShape(Radius.md),
                ),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = char,
              fontSize = 22.sp,
              fontWeight = FontWeight.SemiBold,
              color = colors.textPrimary,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    },
  )
}
