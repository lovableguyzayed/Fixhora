package com.example.ui.screens.mytasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.ui.components.EmptyState
import com.example.ui.components.FixCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusTone
import com.example.ui.format.budgetText
import com.example.ui.format.customerStatusLabel
import com.example.ui.format.taskLocationText
import com.example.ui.format.relativeTimeText
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

/**
 * Everything this customer has asked for.
 *
 * The gap this fills: posting a task used to end at a success screen and the task was then
 * invisible to the person who posted it. There was no way to see whether anyone had picked it up,
 * no way to call it off, and no way to reach the helper who took it.
 */
@Composable
fun MyTasksScreen(tasks: List<TaskEntity>, onOpenTask: (Int) -> Unit, onPostTask: () -> Unit) {
  if (tasks.isEmpty()) {
    EmptyState(
      icon = Icons.AutoMirrored.Filled.Assignment,
      title = "No tasks yet",
      description =
        "Anything you post shows up here, with its status and a way to message the helper who " +
          "takes it on.",
      actionText = "Post a task",
      onAction = onPostTask,
      modifier = Modifier.fillMaxSize(),
    )
    return
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(Spacing.lg),
    verticalArrangement = Arrangement.spacedBy(Spacing.md),
  ) {
    items(tasks, key = { it.id }) { task -> MyTaskCard(task = task, onClick = { onOpenTask(task.id) }) }
  }
}

@Composable
private fun MyTaskCard(task: TaskEntity, onClick: () -> Unit) {
  val colors = FixTheme.colors
  val title = task.descriptionTitle.ifBlank { "Untitled task" }

  FixCard(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(Radius.lg))
        .clickable(role = Role.Button, onClick = onClick)
  ) {
    Column(modifier = Modifier.padding(Spacing.lg)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Text(
          text = title,
          fontWeight = FontWeight.SemiBold,
          color = colors.textPrimary,
          modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.size(Spacing.sm))
        StatusBadge(text = customerStatusLabel(task.status), tone = task.status.customerTone())
      }

      Spacer(Modifier.height(Spacing.md))

      MetaRow(Icons.Default.Schedule, "Posted ${relativeTimeText(task.createdAt)}")
      MetaRow(Icons.Default.LocationOn, taskLocationText(task.locationQuery, task.latitude != null))
      MetaRow(Icons.Default.Payments, budgetText(task.minBudget, task.maxBudget))

      if (task.acceptedByHelperId != null) {
        MetaRow(Icons.AutoMirrored.Filled.Chat, "Tap to message your helper")
      }
    }
  }
}

@Composable
private fun MetaRow(icon: ImageVector, text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(top = Spacing.xs),
  ) {
    Icon(
      imageVector = icon,
      // Decorative: the text beside it says the same thing, and announcing both would repeat it.
      contentDescription = null,
      tint = FixTheme.colors.textSecondary,
      modifier = Modifier.size(16.dp),
    )
    Spacer(Modifier.size(Spacing.sm))
    Text(text = text, color = FixTheme.colors.textSecondary)
  }
}

/**
 * Colour for a status badge.
 *
 * The badge always carries its label too, so this only reinforces what the text already says —
 * colour is never the only carrier of the state.
 */
internal fun TaskStatus.customerTone(): StatusTone =
  when (this) {
    TaskStatus.DRAFT -> StatusTone.NEUTRAL
    TaskStatus.SUBMITTED -> StatusTone.INFO
    TaskStatus.ACCEPTED -> StatusTone.INFO
    TaskStatus.IN_PROGRESS -> StatusTone.WARNING
    TaskStatus.COMPLETED -> StatusTone.SUCCESS
    TaskStatus.CANCELLED -> StatusTone.DANGER
    TaskStatus.REJECTED -> StatusTone.NEUTRAL
  }
