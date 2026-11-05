package com.example.ui.screens.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.room.TaskEntity
import com.example.data.room.fullAddress
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoTag
import com.example.ui.components.CircleIconButton
import com.example.ui.format.budgetText
import com.example.ui.format.relativeTimeText
import com.example.ui.format.scheduleText
import com.example.ui.format.taskLocationText
import com.example.ui.screens.taskflow.categoryBento
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.BentoColors
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Spacing

/** Text on the white info strip inside a job card; the strip is white in both themes. */
private val StripInk = Color(0xFF141416)

/**
 * A job as a card in its category's colour: what and where on the card itself, who and how much
 * on a white strip, and the actions beneath.
 *
 * Shared by the Home, Map and Tasks tabs, which each drew their own white card before. [onChat]
 * puts the round chat button in the top corner, labelled [chatLabel] for screen readers.
 */
@Composable
fun JobBentoCard(
  task: TaskEntity,
  posterName: String,
  statusLabel: String,
  modifier: Modifier = Modifier,
  onChat: (() -> Unit)? = null,
  chatLabel: String = "",
  showFullAddress: Boolean = false,
  /** Rings the customer; set only once this helper holds the job and the customer has a number. */
  onCall: (() -> Unit)? = null,
  callLabel: String = "",
  actions: @Composable ColumnScope.(BentoColors) -> Unit,
) {
  val bento = categoryBento(task.categoryId)
  val category = dummyCategories.find { it.id == task.categoryId }
  // Recomputed per composition rather than captured once, so the age does not freeze on screen.
  val postedAgo = relativeTimeText(task.createdAt)
  val categoryTitle = category?.let { stringResource(it.titleRes) } ?: stringResource(R.string.task_uncategorised)

  BentoCard(colors = bento, modifier = modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          BentoTag(text = categoryTitle, colors = bento)
          Spacer(Modifier.width(Spacing.sm))
          BentoTag(text = statusLabel, colors = bento)
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(postedAgo, style = MaterialTheme.typography.bodySmall, color = bento.onFillSecondary)
      }
      if (onCall != null) {
        CircleIconButton(
          icon = Icons.Default.Call,
          contentDescription = callLabel,
          onClick = onCall,
          container = FixTheme.colors.ink,
          content = FixTheme.colors.onInk,
        )
        if (onChat != null) Spacer(Modifier.width(Spacing.sm))
      }
      if (onChat != null) {
        CircleIconButton(
          icon = Icons.AutoMirrored.Filled.Chat,
          contentDescription = chatLabel,
          onClick = onChat,
          container = FixTheme.colors.ink,
          content = FixTheme.colors.onInk,
        )
      }
    }

    Spacer(Modifier.height(Spacing.md))
    Text(
      text = task.descriptionTitle.ifBlank { categoryTitle },
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.ExtraBold,
      color = bento.onFill,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
    if (task.descriptionDetails.isNotBlank()) {
      Spacer(Modifier.height(Spacing.xs))
      Text(
        text = task.descriptionDetails,
        style = MaterialTheme.typography.bodyMedium,
        color = bento.onFillSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(Modifier.height(Spacing.md))
    // When first: it decides whether a helper can take the job at all.
    JobMeta(Icons.Default.Event, scheduleText(task.scheduledDate, task.timeSlot), bento, emphasise = true)
    Spacer(Modifier.height(Spacing.xs))
    JobMeta(Icons.Default.LocationOn, taskLocationText(task.locationQuery, task.latitude != null), bento)
    val address = fullAddress(task)
    if (address != null) {
      Spacer(Modifier.height(Spacing.xs))
      if (showFullAddress) {
        JobMeta(Icons.Default.Home, stringResource(R.string.job_full_address, address), bento, emphasise = true)
      } else {
        JobMeta(Icons.Default.Lock, stringResource(R.string.job_address_after_accept), bento)
      }
    }
    Spacer(Modifier.height(Spacing.xs))
    // The customer's search radius, worded as theirs: "Within 5 km" read as the helper's own
    // distance from the job, which the app does not know.
    JobMeta(Icons.Default.MyLocation, stringResource(R.string.job_radius, task.selectedDistance), bento)

    Spacer(Modifier.height(Spacing.md))
    // Who and how much, on a white strip — the one place a helper's eye goes first.
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.9f))
          .padding(start = Spacing.xs, end = Spacing.lg, top = Spacing.xs, bottom = Spacing.xs),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        Icons.Default.Person,
        contentDescription = null,
        tint = StripInk,
        modifier = Modifier.size(32.dp).clip(CircleShape).background(bento.fill).padding(6.dp),
      )
      Spacer(Modifier.width(Spacing.sm))
      // Was "Customer Name" with a verified tick and a 4.8 rating. Nothing verifies anyone and
      // there is no rating system, so neither is shown.
      Text(
        posterName,
        style = MaterialTheme.typography.labelLarge,
        color = StripInk,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(Spacing.sm))
      Text(
        budgetText(task.minBudget, task.maxBudget),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = StripInk,
        maxLines = 1,
      )
    }

    Spacer(Modifier.height(Spacing.md))
    actions(bento)
  }
}

@Composable
private fun JobMeta(icon: ImageVector, text: String, colors: BentoColors, emphasise: Boolean = false) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, contentDescription = null, tint = colors.onFill, modifier = Modifier.size(16.dp))
    Spacer(Modifier.width(Spacing.sm))
    Text(
      text,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = if (emphasise) FontWeight.Bold else FontWeight.Normal,
      color = colors.onFill,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
