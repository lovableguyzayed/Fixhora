package com.example.ui.screens.mytasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.repository.ChatRepository
import com.example.data.room.CancelReason
import com.example.data.room.ChatMessageEntity
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.data.room.fullAddress
import com.example.ui.components.BentoAction
import com.example.ui.components.BentoActionRow
import com.example.ui.components.BentoCard
import com.example.ui.components.BentoTag
import com.example.ui.components.ChatInputBar
import com.example.ui.components.CircleIconButton
import com.example.ui.components.FixTopBar
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.MessageBubble
import com.example.ui.components.PillChip
import com.example.ui.format.awaitsCustomerConfirmation
import com.example.ui.format.budgetText
import com.example.ui.format.customerStatusDetail
import com.example.ui.format.customerStatusLabel
import com.example.ui.format.hasAssignedHelper
import com.example.ui.format.isCancellableByCustomer
import com.example.ui.format.isDeletableByCustomer
import com.example.ui.format.label
import com.example.ui.format.ratingText
import com.example.ui.format.relativeTimeText
import com.example.ui.format.scheduleText
import com.example.ui.format.taskLocationText
import com.example.ui.screens.taskflow.categoryBento
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.Bento
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing
import com.example.util.dialMobile

/**
 * One of the customer's tasks: what it says, where it stands, who took it, and the conversation.
 *
 * The chat lives here rather than in its own tab because a customer's message is always *about* a
 * task. The worker side needs a thread list because it deals with many customers; the customer is
 * only ever talking about the job in front of them.
 *
 * The summary scrolls with the conversation so a long chat cannot squeeze it off the screen; the
 * helper's name and call button stay pinned above, because reaching them is what this screen is
 * most often opened for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTaskDetailScreen(
  task: TaskEntity,
  messages: List<ChatMessageEntity>,
  onBack: () -> Unit,
  onSend: (String) -> Unit,
  onCancelTask: (CancelReason?) -> Unit,
  onDeleteTask: () -> Unit = {},
  helper: AssignedHelper? = null,
  onConfirmCompletion: (rating: Int, review: String) -> Unit = { _, _ -> },
  onReopenTask: () -> Unit = {},
) {
  val colors = FixTheme.colors
  val context = LocalContext.current
  var draft by rememberSaveable { mutableStateOf("") }
  var confirmingCancel by rememberSaveable { mutableStateOf(false) }
  var confirmingDelete by rememberSaveable { mutableStateOf(false) }

  if (confirmingDelete) {
    AlertDialog(
      onDismissRequest = { confirmingDelete = false },
      containerColor = colors.surface,
      titleContentColor = colors.textPrimary,
      textContentColor = colors.textSecondary,
      title = { Text(stringResource(R.string.mytasks_delete_title), fontWeight = FontWeight.SemiBold) },
      text = { Text(stringResource(R.string.mytasks_delete_body)) },
      confirmButton = {
        TextButton(
          onClick = {
            confirmingDelete = false
            onDeleteTask()
          }
        ) {
          Text(stringResource(R.string.mytasks_delete_confirm), color = colors.danger, fontWeight = FontWeight.SemiBold)
        }
      },
      dismissButton = {
        TextButton(onClick = { confirmingDelete = false }) {
          Text(stringResource(R.string.mytasks_cancel_keep), color = colors.textSecondary)
        }
      },
    )
  }

  if (confirmingCancel) {
    CancelTaskDialog(
      onDismiss = { confirmingCancel = false },
      onConfirm = { reason ->
        confirmingCancel = false
        onCancelTask(reason)
      },
    )
  }

  // A helper may ask a question before taking the job. That thread used to be invisible here until
  // someone accepted, so the question went unanswered; any message now opens the conversation.
  val chatOpen = hasAssignedHelper(task.status) || messages.isNotEmpty()
  val showHelper = hasAssignedHelper(task.status) && task.acceptedByHelperId != null

  Scaffold(
    containerColor = colors.background,
    topBar = {
      FixTopBar(
        title = task.descriptionTitle.ifBlank { stringResource(R.string.task_untitled) },
        onBack = onBack,
        backDescription = stringResource(R.string.cd_back_to_my_tasks),
      )
    },
  ) { padding ->
    // consumeWindowInsets before imePadding: the keyboard's inset includes the navigation bar
    // that padding already covers, so without it the input row floats a bar's height too high.
    Column(modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
      if (showHelper) {
        HelperStrip(
          helper = helper,
          onCall = { mobile -> context.dialMobile(mobile) },
          modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.sm),
        )
      }

      LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        // Newest first, matching the DAO's ORDER BY timestamp DESC, so the list opens on the latest
        // message. Top-aligned so a short thread sits under the summary instead of at the bottom.
        reverseLayout = true,
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
      ) {
        if (awaitsCustomerConfirmation(task)) {
          item(key = "confirm") {
            ConfirmCompletionCard(
              helperName = helper?.name,
              onConfirm = onConfirmCompletion,
              onReopen = onReopenTask,
              modifier = Modifier.padding(top = Spacing.sm),
            )
          }
        }

        items(messages, key = { it.id }) { message ->
          MessageBubble(
            text = message.text,
            isSender = message.senderId == ChatRepository.SENDER_CUSTOMER,
            time = relativeTimeText(message.timestamp),
          )
        }

        if (messages.isEmpty()) {
          item(key = "chat-note") {
            Text(
              // No thread before a helper is assigned: there is nobody on the other end, and an
              // empty input would imply otherwise.
              text = stringResource(if (chatOpen) R.string.mytasks_chat_empty else R.string.mytasks_chat_locked),
              color = colors.textSecondary,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xl, horizontal = Spacing.sm),
            )
          }
        }

        item(key = "summary") {
          TaskSummary(
            task = task,
            onRequestCancel = { confirmingCancel = true },
            onRequestDelete = { confirmingDelete = true },
            modifier = Modifier.padding(bottom = Spacing.sm),
          )
        }
      }

      if (chatOpen) {
        ChatInputBar(
          value = draft,
          onValueChange = { draft = it },
          placeholder = stringResource(R.string.mytasks_chat_hint),
          onSend = {
            onSend(it)
            draft = ""
          },
        )
      }
    }
  }
}

/**
 * Who took the job. The card said only "A helper has taken this on", so the customer could not tell
 * who would turn up at their door, or ring them if they were late.
 */
@Composable
private fun HelperStrip(helper: AssignedHelper?, onCall: (String) -> Unit, modifier: Modifier = Modifier) {
  val colors = FixTheme.colors
  val name = helper?.name
  val displayName = name ?: stringResource(R.string.helper_guest_name)
  BentoCard(colors = Bento.Cream, modifier = modifier.fillMaxWidth(), contentPadding = Spacing.md) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      InitialsAvatar(name = name, size = 44.dp)
      Spacer(Modifier.width(Spacing.md))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(R.string.helper_card_title),
          style = MaterialTheme.typography.labelMedium,
          color = Bento.Cream.onFillSecondary,
        )
        Text(
          displayName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = Bento.Cream.onFill,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        val skills = helper?.skills.orEmpty().mapNotNull { id -> dummyCategories.firstOrNull { it.id == id } }
        val details = listOf(ratingText(helper?.rating)) + skills.take(2).map { stringResource(it.titleRes) }
        Text(
          details.joinToString(" · "),
          style = MaterialTheme.typography.bodySmall,
          color = Bento.Cream.onFillSecondary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
      val mobile = helper?.mobile
      if (mobile != null) {
        Spacer(Modifier.width(Spacing.sm))
        CircleIconButton(
          icon = Icons.Default.Call,
          contentDescription = stringResource(R.string.cd_call_name, displayName),
          onClick = { onCall(mobile) },
          container = colors.ink,
          content = colors.onInk,
        )
      }
    }
    helper?.about?.let { about ->
      Spacer(Modifier.height(Spacing.sm))
      Text(
        about,
        style = MaterialTheme.typography.bodySmall,
        color = Bento.Cream.onFillSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/** Five tappable stars. Each is a radio choice, so a screen reader announces "3 of 5 stars". */
@Composable
private fun StarPicker(rating: Int, onRate: (Int) -> Unit, tint: androidx.compose.ui.graphics.Color) {
  Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
    for (star in 1..5) {
      val description = stringResource(R.string.cd_rate_stars, star)
      Icon(
        imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
        contentDescription = null,
        tint = tint,
        modifier =
          Modifier.size(MinTouchTarget)
            .selectable(selected = star == rating, role = Role.RadioButton, onClick = { onRate(star) })
            .semantics { contentDescription = description }
            .padding(Spacing.xs),
      )
    }
  }
}

/**
 * The helper says the job is done; the customer says whether it is, and how it went. Until now a
 * job was finished whenever the helper said so, and nobody could ever say how it went.
 */
@Composable
private fun ConfirmCompletionCard(
  helperName: String?,
  onConfirm: (Int, String) -> Unit,
  onReopen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bento = Bento.Lime
  var rating by rememberSaveable { mutableIntStateOf(0) }
  var review by rememberSaveable { mutableStateOf("") }
  BentoCard(colors = bento, modifier = modifier.fillMaxWidth()) {
    Text(
      stringResource(R.string.confirm_title),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.ExtraBold,
      color = bento.onFill,
    )
    Spacer(Modifier.height(Spacing.xs))
    Text(
      stringResource(R.string.confirm_body, helperName ?: stringResource(R.string.helper_fallback_name)),
      style = MaterialTheme.typography.bodyMedium,
      color = bento.onFillSecondary,
    )
    Spacer(Modifier.height(Spacing.sm))
    StarPicker(rating = rating, onRate = { rating = it }, tint = bento.onFill)
    Spacer(Modifier.height(Spacing.sm))
    OutlinedTextField(
      value = review,
      onValueChange = { review = it.take(MyTasksViewModel.MAX_REVIEW_LENGTH) },
      placeholder = { Text(stringResource(R.string.review_hint), color = bento.onFillSecondary) },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(Radius.md),
      maxLines = 3,
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedTextColor = bento.onFill,
          unfocusedTextColor = bento.onFill,
          focusedBorderColor = bento.onFill,
          unfocusedBorderColor = bento.onFill.copy(alpha = 0.4f),
          cursorColor = bento.onFill,
        ),
    )
    Spacer(Modifier.height(Spacing.md))
    // One per row: side by side, "Confirm & rate" was cut to "Confirm …".
    BentoActionRow {
      BentoAction(
        stringResource(R.string.action_confirm_rate),
        { if (rating in 1..5) onConfirm(rating, review) },
        bento,
        filled = rating in 1..5,
        icon = Icons.Default.Star,
      )
    }
    Spacer(Modifier.height(Spacing.sm))
    BentoActionRow { BentoAction(stringResource(R.string.action_not_done), onReopen, bento, filled = false) }
  }
}

/** "Called off" was all a customer could say; a reason takes one tap and tells the helper why. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CancelTaskDialog(onDismiss: () -> Unit, onConfirm: (CancelReason?) -> Unit) {
  val colors = FixTheme.colors
  var reason by rememberSaveable { mutableStateOf<CancelReason?>(null) }
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = colors.surface,
    titleContentColor = colors.textPrimary,
    textContentColor = colors.textSecondary,
    title = { Text(stringResource(R.string.mytasks_cancel_title), fontWeight = FontWeight.SemiBold) },
    text = {
      Column {
        Text(stringResource(R.string.mytasks_cancel_body))
        Spacer(Modifier.height(Spacing.lg))
        Text(
          stringResource(R.string.cancel_reason_label),
          style = MaterialTheme.typography.titleSmall,
          color = colors.textPrimary,
        )
        Spacer(Modifier.height(Spacing.sm))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
          verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
          CancelReason.entries.forEach { option ->
            PillChip(
              text = option.label(),
              selected = reason == option,
              onClick = { reason = if (reason == option) null else option },
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = { onConfirm(reason) }) {
        Text(stringResource(R.string.mytasks_cancel_confirm), color = colors.danger, fontWeight = FontWeight.SemiBold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.mytasks_cancel_keep), color = colors.textSecondary)
      }
    },
  )
}

@Composable
private fun TaskSummary(
  task: TaskEntity,
  onRequestCancel: () -> Unit,
  onRequestDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bento = categoryBento(task.categoryId)
  BentoCard(colors = bento, modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BentoTag(
        text =
          if (awaitsCustomerConfirmation(task)) stringResource(R.string.status_confirm_needed)
          else customerStatusLabel(task.status),
        colors = bento,
      )
      Text(
        text = relativeTimeText(task.createdAt),
        style = MaterialTheme.typography.bodySmall,
        color = bento.onFillSecondary,
      )
    }

    Spacer(Modifier.height(Spacing.sm))
    Text(
      text =
        if (task.status == TaskStatus.COMPLETED && task.confirmedAt != null) {
          stringResource(R.string.status_detail_confirmed)
        } else {
          customerStatusDetail(task.status)
        },
      style = MaterialTheme.typography.bodyMedium,
      color = bento.onFill,
    )
    CancelReason.fromStorage(task.cancelReason)
      ?.takeIf { task.status == TaskStatus.CANCELLED }
      ?.let { reason ->
        Text(
          text = stringResource(R.string.cancel_reason_shown, reason.label()),
          style = MaterialTheme.typography.bodyMedium,
          color = bento.onFillSecondary,
        )
      }

    if (task.descriptionDetails.isNotBlank()) {
      Spacer(Modifier.height(Spacing.md))
      Text(text = task.descriptionDetails, style = MaterialTheme.typography.bodyMedium, color = bento.onFillSecondary)
    }

    Spacer(Modifier.height(Spacing.md))
    Text(
      text = scheduleText(task.scheduledDate, task.timeSlot),
      style = MaterialTheme.typography.titleSmall,
      color = bento.onFill,
    )
    Text(
      text = taskLocationText(task.locationQuery, task.latitude != null),
      style = MaterialTheme.typography.bodyMedium,
      color = bento.onFillSecondary,
    )
    fullAddress(task)?.let {
      Text(text = it, style = MaterialTheme.typography.bodyMedium, color = bento.onFillSecondary)
    }
    Text(
      text = budgetText(task.minBudget, task.maxBudget),
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.ExtraBold,
      color = bento.onFill,
    )

    val given = task.rating
    if (given != null && given in 1..5) {
      Spacer(Modifier.height(Spacing.md))
      Text(
        stringResource(R.string.your_rating),
        style = MaterialTheme.typography.labelMedium,
        color = bento.onFillSecondary,
      )
      val starsDescription = stringResource(R.string.cd_rate_stars, given)
      Text(
        "★".repeat(given) + "☆".repeat(5 - given),
        style = MaterialTheme.typography.titleMedium,
        color = bento.onFill,
        modifier = Modifier.semantics { contentDescription = starsDescription },
      )
      task.review?.let {
        Text("“$it”", style = MaterialTheme.typography.bodyMedium, color = bento.onFillSecondary)
      }
    }

    val canCancel = isCancellableByCustomer(task.status)
    val canDelete = isDeletableByCustomer(task.status)
    if (canCancel || canDelete) {
      // One action per row: side by side, "Cancel this task" and "Delete task" were both cut off.
      Spacer(Modifier.height(Spacing.lg))
      if (canCancel) {
        BentoActionRow {
          BentoAction(stringResource(R.string.mytasks_cancel_action), onRequestCancel, bento, filled = false)
        }
      }
      if (canCancel && canDelete) Spacer(Modifier.height(Spacing.sm))
      if (canDelete) {
        BentoActionRow {
          BentoAction(
            stringResource(R.string.mytasks_delete_action),
            onRequestDelete,
            bento,
            filled = true,
            icon = Icons.Default.DeleteOutline,
          )
        }
      }
    }
  }
}
