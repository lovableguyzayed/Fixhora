package com.example.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BentoColors
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

/**
 * A coloured card: one flat fill, large soft corners, no border and no shadow.
 *
 * The colour does the work a border would, so cards sit straight on the page. When [onClick] is
 * set the whole card is the touch target.
 */
@Composable
fun BentoCard(
  colors: BentoColors,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  contentPadding: androidx.compose.ui.unit.Dp = Spacing.lg,
  content: @Composable ColumnScope.() -> Unit,
) {
  val shape = RoundedCornerShape(Radius.xl)
  Column(
    modifier =
      modifier
        .clip(shape)
        .background(colors.fill)
        .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
        .padding(contentPadding),
    content = content,
  )
}

/**
 * A number with its label on a coloured card — the big-figure tiles of the home screens.
 *
 * [large] is for the one figure a screen leads with; the rest sit beside each other at the
 * smaller size.
 */
@Composable
fun BentoStat(
  value: String,
  label: String,
  colors: BentoColors,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  caption: String? = null,
  large: Boolean = false,
) {
  BentoCard(colors = colors, modifier = modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = colors.onFillSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
      if (icon != null) {
        Icon(icon, contentDescription = null, tint = colors.onFill, modifier = Modifier.size(20.dp))
      }
    }
    Spacer(Modifier.height(if (large) Spacing.md else Spacing.sm))
    Text(
      text = value,
      style = if (large) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displaySmall,
      color = colors.onFill,
      maxLines = 1,
    )
    if (caption != null) {
      Spacer(Modifier.height(Spacing.xs))
      Text(text = caption, style = MaterialTheme.typography.bodySmall, color = colors.onFillSecondary)
    }
  }
}

/** A small label on a coloured card: the card's own text colour on a slightly deeper tint. */
@Composable
fun BentoTag(text: String, colors: BentoColors, modifier: Modifier = Modifier) {
  Text(
    text = text,
    style = MaterialTheme.typography.labelMedium,
    color = colors.onFill,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier =
      modifier
        .clip(CircleShape)
        .background(colors.onFill.copy(alpha = 0.1f))
        .padding(horizontal = Spacing.md, vertical = Spacing.xs),
  )
}

/**
 * A rounded chip for filters and quick choices. The selected one turns the highlight yellow, the
 * same colour as the active tab, so "this one is on" reads the same everywhere.
 */
@Composable
fun PillChip(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
) {
  val colors = FixTheme.colors
  Surface(
    modifier =
      modifier
        .heightIn(min = 40.dp)
        .clip(CircleShape)
        .selectable(selected = selected, role = Role.Tab, onClick = onClick),
    shape = CircleShape,
    color = if (selected) colors.highlight else colors.surface,
    border = if (selected) null else BorderStroke(1.dp, colors.border),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (icon != null) {
        Icon(
          icon,
          contentDescription = null,
          tint = if (selected) colors.onHighlight else colors.textSecondary,
          modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(Spacing.xs))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) colors.onHighlight else colors.textPrimary,
        maxLines = 1,
      )
    }
  }
}

/**
 * A round icon button with a filled background — the black circle on a coloured card, or a
 * white one on a dark card. 48dp, so it is a full touch target on its own.
 */
@Composable
fun CircleIconButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  container: Color = FixTheme.colors.ink,
  content: Color = FixTheme.colors.onInk,
  size: androidx.compose.ui.unit.Dp = MinTouchTarget,
) {
  Box(
    modifier =
      modifier
        .size(size)
        .clip(CircleShape)
        .background(container)
        .clickable(role = Role.Button, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(icon, contentDescription = contentDescription, tint = content, modifier = Modifier.size(22.dp))
  }
}

/** One tab of a [FloatingNavBar]. */
data class FloatingNavItem(@StringRes val labelRes: Int, val icon: ImageVector)

/**
 * The tab bar: a dark rounded bar floating above the content, every tab an icon over its name,
 * with the active one on a yellow pill.
 *
 * Inactive tabs used to show only their icon. A map outline or a clipboard means little to someone
 * new to apps, so every tab now says what it is, in either language. Each tab is a selectable
 * `Role.Tab`. It pads itself for the system navigation bar.
 */
@Composable
fun FloatingNavBar(
  items: List<FloatingNavItem>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = FixTheme.colors
  val barShape = RoundedCornerShape(Radius.xl)
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.background)
        .navigationBarsPadding()
        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .clip(barShape)
          .background(colors.ink)
          .border(1.dp, colors.onInk.copy(alpha = 0.06f), barShape)
          .padding(Spacing.xs),
      horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      items.forEachIndexed { index, item ->
        val selected = index == selectedIndex
        val content = if (selected) colors.onHighlight else colors.onInk.copy(alpha = 0.78f)
        Column(
          modifier =
            Modifier.weight(1f)
              .heightIn(min = 56.dp)
              .clip(RoundedCornerShape(Radius.lg))
              .background(if (selected) colors.highlight else Color.Transparent)
              .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
              .padding(vertical = Spacing.xs, horizontal = Spacing.xs),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Icon(item.icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
          Spacer(Modifier.height(2.dp))
          Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

/**
 * An action on a coloured card. [filled] is the black pill for the main action; otherwise an
 * outline in the card's text colour, which reads on every card colour where a blue or grey
 * button would not.
 */
@Composable
fun RowScope.BentoAction(
  text: String,
  onClick: () -> Unit,
  colors: BentoColors,
  filled: Boolean,
  icon: ImageVector? = null,
) {
  val modifier = Modifier.weight(1f).heightIn(min = MinTouchTarget)
  if (filled) {
    Button(
      onClick = onClick,
      modifier = modifier,
      shape = CircleShape,
      colors =
        ButtonDefaults.buttonColors(
          containerColor = FixTheme.colors.ink,
          contentColor = FixTheme.colors.onInk,
        ),
    ) {
      ActionLabel(text, icon)
    }
  } else {
    OutlinedButton(
      onClick = onClick,
      modifier = modifier,
      shape = CircleShape,
      border = BorderStroke(1.5.dp, colors.onFill.copy(alpha = 0.5f)),
      colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onFill),
    ) {
      ActionLabel(text, icon)
    }
  }
}

@Composable
private fun ActionLabel(text: String, icon: ImageVector?) {
  if (icon != null) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(Spacing.xs))
  }
  Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** Row of [BentoAction]s with even gaps. */
@Composable
fun BentoActionRow(content: @Composable RowScope.() -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    verticalAlignment = Alignment.CenterVertically,
    content = content,
  )
}
