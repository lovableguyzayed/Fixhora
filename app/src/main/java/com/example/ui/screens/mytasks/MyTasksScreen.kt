package com.example.ui.screens.mytasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoTag
import com.example.ui.components.EmptyState
import com.example.ui.components.FixTopBar
import com.example.ui.components.PillChip
import com.example.ui.format.budgetText
import com.example.ui.format.customerStatusLabel
import com.example.ui.format.relativeTimeText
import com.example.ui.format.scheduleText
import com.example.ui.format.taskLocationText
import com.example.ui.screens.taskflow.categoryBento
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.BentoColors
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Spacing

/**
 * Everything this customer has asked for.
 *
 * The gap this fills: posting a task used to end at a success screen and the task was then
 * invisible to the person who posted it. There was no way to see whether anyone had picked it up,
 * no way to call it off, and no way to reach the helper who took it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(
  tasks: List<TaskEntity>,
  onOpenTask: (Int) -> Unit,
  onPostTask: () -> Unit,
  onBack: () -> Unit = {},
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // This tab had no heading and no way back to the role choice except the system back gesture.
    FixTopBar(
      title = stringResource(R.string.customer_tab_my_tasks),
      onBack = onBack,
      actions = {
        if (tasks.isNotEmpty()) {
          PillChip(
            text = stringResource(R.string.mytasks_post_a_task),
            selected = true,
            onClick = onPostTask,
            icon = Icons.Default.Add,
            modifier = Modifier.padding(end = Spacing.lg),
          )
        }
      },
    )

    if (tasks.isEmpty()) {
      EmptyState(
        icon = Icons.AutoMirrored.Filled.Assignment,
        title = stringResource(R.string.mytasks_empty_title),
        description = stringResource(R.string.mytasks_empty_body),
        actionText = stringResource(R.string.mytasks_post_a_task),
        onAction = onPostTask,
        modifier = Modifier.fillMaxSize(),
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, top = Spacing.sm, bottom = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
      ) {
        items(tasks, key = { it.id }) { task -> MyTaskCard(task = task, onClick = { onOpenTask(task.id) }) }
      }
    }
  }
}

/** One posted task, as a card in its category's colour. The whole card opens the task. */
@Composable
private fun MyTaskCard(task: TaskEntity, onClick: () -> Unit) {
  val colors = FixTheme.colors
  val bento = categoryBento(task.categoryId)
  val category = dummyCategories.find { it.id == task.categoryId }
  val title = task.descriptionTitle.ifBlank { stringResource(R.string.task_untitled) }

  BentoCard(colors = bento, modifier = Modifier.fillMaxWidth(), onClick = onClick) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        if (category != null) {
          BentoTag(text = stringResource(category.titleRes), colors = bento)
          Spacer(Modifier.width(Spacing.sm))
        }
        BentoTag(text = customerStatusLabel(task.status), colors = bento)
      }
      // Marks the card as something that opens; the card itself is the touch target.
      Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.ink),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          tint = colors.onInk,
          modifier = Modifier.size(20.dp),
        )
      }
    }

    Spacer(Modifier.height(Spacing.md))
    Text(
      text = title,
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.ExtraBold,
      color = bento.onFill,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(Spacing.xs))
    MetaRow(
      Icons.Default.Schedule,
      stringResource(R.string.mytasks_posted_ago, relativeTimeText(task.createdAt)),
      bento,
    )
    MetaRow(Icons.Default.Event, scheduleText(task.scheduledDate, task.timeSlot), bento)
    MetaRow(Icons.Default.LocationOn, taskLocationText(task.locationQuery, task.latitude != null), bento)

    Spacer(Modifier.height(Spacing.md))
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.9f))
          .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(Icons.Default.Payments, contentDescription = null, tint = StripInk, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(Spacing.sm))
      Text(
        text = budgetText(task.minBudget, task.maxBudget),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = StripInk,
        modifier = Modifier.weight(1f),
      )
      if (task.acceptedByHelperId != null) {
        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = StripInk, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(Spacing.xs))
        Text(
          stringResource(R.string.mytasks_tap_to_message),
          style = MaterialTheme.typography.labelMedium,
          color = StripInk,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

/** Text on the white strip, which is white in both themes. */
private val StripInk = Color(0xFF141416)

@Composable
private fun MetaRow(icon: ImageVector, text: String, bento: BentoColors) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(top = Spacing.xs),
  ) {
    Icon(
      imageVector = icon,
      // Decorative: the text beside it says the same thing, and announcing both would repeat it.
      contentDescription = null,
      tint = bento.onFill,
      modifier = Modifier.size(16.dp),
    )
    Spacer(Modifier.size(Spacing.sm))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = bento.onFillSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
