package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

/** Visual weight of a [FixButton], matching how consequential the action is. */
enum class FixButtonStyle {
  /** The one action the screen exists for. */
  PRIMARY,
  /** A secondary path that is still a real choice. */
  SECONDARY,
  /** Destructive, and not undoable. */
  DANGER,
}

/**
 * The app's button.
 *
 * Exists because every screen was hand-rolling `Button(height = 56.dp, shape =
 * RoundedCornerShape(12.dp), colors = ...)`, and none of them handled a loading state — which is
 * why several actions could be fired twice.
 */
@Composable
fun FixButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  style: FixButtonStyle = FixButtonStyle.PRIMARY,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  loadingText: String = "Please wait…",
) {
  val colors = FixTheme.colors
  val shape = RoundedCornerShape(Radius.md)
  // A button already working must not be pressable again, whatever the caller passed.
  val clickable = enabled && !isLoading
  val height = MinTouchTarget + Spacing.sm

  if (style == FixButtonStyle.SECONDARY) {
    OutlinedButton(
      onClick = onClick,
      enabled = clickable,
      modifier = modifier.fillMaxWidth().height(height),
      shape = shape,
      border = BorderStroke(1.dp, colors.border),
      colors =
        ButtonDefaults.outlinedButtonColors(
          contentColor = colors.textPrimary,
          disabledContentColor = colors.onDisabled,
        ),
    ) {
      ButtonContent(text, isLoading, loadingText, colors.textPrimary)
    }
  } else {
    val container = if (style == FixButtonStyle.DANGER) colors.danger else colors.primary
    Button(
      onClick = onClick,
      enabled = clickable,
      modifier = modifier.fillMaxWidth().height(height),
      shape = shape,
      colors =
        ButtonDefaults.buttonColors(
          containerColor = container,
          contentColor = colors.onPrimary,
          disabledContainerColor = colors.disabled,
          disabledContentColor = colors.onDisabled,
        ),
    ) {
      ButtonContent(text, isLoading, loadingText, colors.onPrimary)
    }
  }
}

@Composable
private fun ButtonContent(
  text: String,
  isLoading: Boolean,
  loadingText: String,
  contentColor: androidx.compose.ui.graphics.Color,
) {
  if (isLoading) {
    CircularProgressIndicator(
      modifier = Modifier.size(20.dp),
      color = contentColor,
      strokeWidth = 2.dp,
    )
    Spacer(modifier = Modifier.width(Spacing.md))
    Text(loadingText, style = MaterialTheme.typography.labelLarge)
  } else {
    Text(text, style = MaterialTheme.typography.labelLarge)
  }
}

/** The app's card: one surface colour, one radius, one border. */
@Composable
fun FixCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(Radius.lg),
    colors = CardDefaults.cardColors(containerColor = FixTheme.colors.surface),
    border = BorderStroke(1.dp, FixTheme.colors.border),
  ) {
    content()
  }
}

/** Tone of a [StatusBadge]. */
enum class StatusTone {
  NEUTRAL,
  INFO,
  SUCCESS,
  WARNING,
  DANGER,
}

/**
 * A status pill.
 *
 * Always carries its own label, so a status is never conveyed by colour alone — which is the one
 * thing a colour-blind user cannot recover.
 */
@Composable
fun StatusBadge(text: String, tone: StatusTone, modifier: Modifier = Modifier) {
  val colors = FixTheme.colors
  val container =
    when (tone) {
      StatusTone.NEUTRAL -> colors.surfaceAlt
      StatusTone.INFO -> colors.infoSurface
      StatusTone.SUCCESS -> colors.successSurface
      StatusTone.WARNING -> colors.warningSurface
      StatusTone.DANGER -> colors.dangerSurface
    }
  val content =
    when (tone) {
      StatusTone.NEUTRAL -> colors.textSecondary
      StatusTone.INFO -> colors.info
      StatusTone.SUCCESS -> colors.success
      StatusTone.WARNING -> colors.warning
      StatusTone.DANGER -> colors.danger
    }
  Surface(color = container, shape = RoundedCornerShape(Radius.sm), modifier = modifier) {
    Text(
      text = text,
      color = content,
      style = MaterialTheme.typography.labelMedium,
      modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
    )
  }
}

/**
 * What a screen shows instead of nothing.
 *
 * A blank screen leaves the user unable to tell whether the app is broken, still loading, or
 * genuinely has nothing to show, so every empty state names the situation and offers a way out.
 */
@Composable
fun EmptyState(
  icon: ImageVector,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  actionText: String? = null,
  onAction: (() -> Unit)? = null,
) {
  val colors = FixTheme.colors
  Column(
    modifier = modifier.fillMaxWidth().padding(Spacing.xl),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = colors.textMuted,
      modifier = Modifier.size(56.dp),
    )
    Spacer(modifier = Modifier.height(Spacing.lg))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = colors.textPrimary,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(Spacing.sm))
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = colors.textSecondary,
      textAlign = TextAlign.Center,
    )
    if (actionText != null && onAction != null) {
      Spacer(modifier = Modifier.height(Spacing.xl))
      FixButton(text = actionText, onClick = onAction, style = FixButtonStyle.SECONDARY)
    }
  }
}

/**
 * A pulsing placeholder for content that is still loading.
 *
 * The pulse is the point: a static grey rectangle is indistinguishable from a broken image.
 */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier, height: Dp = 16.dp, cornerRadius: Dp = Radius.sm) {
  val transition = rememberInfiniteTransition(label = "skeleton")
  val pulse by
    transition.animateFloat(
      initialValue = 0.35f,
      targetValue = 0.75f,
      animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
      label = "skeletonAlpha",
    )
  Box(
    modifier =
      modifier
        .height(height)
        .alpha(pulse)
        .background(FixTheme.colors.border, RoundedCornerShape(cornerRadius))
  )
}

/** A section heading with an optional trailing action, so headings stay consistent. */
@Composable
fun SectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  trailing: @Composable (() -> Unit)? = null,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      color = FixTheme.colors.textPrimary,
    )
    trailing?.invoke()
  }
}
