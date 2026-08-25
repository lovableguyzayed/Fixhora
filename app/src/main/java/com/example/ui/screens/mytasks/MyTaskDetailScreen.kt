package com.example.ui.screens.mytasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.repository.ChatRepository
import com.example.data.room.ChatMessageEntity
import com.example.data.room.TaskEntity
import com.example.ui.components.FixButton
import com.example.ui.components.FixButtonStyle
import com.example.ui.components.FixCard
import com.example.ui.components.StatusBadge
import com.example.ui.format.budgetText
import com.example.ui.format.customerStatusDetail
import com.example.ui.format.customerStatusLabel
import com.example.ui.format.hasAssignedHelper
import com.example.ui.format.isCancellableByCustomer
import com.example.ui.format.taskLocationText
import com.example.ui.format.relativeTimeText
import com.example.ui.components.MessageBubble
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

/**
 * One of the customer's tasks: what it says, where it stands, and the conversation on it.
 *
 * The chat lives here rather than in its own tab because a customer's message is always *about* a
 * task. The worker side needs a thread list because it deals with many customers; the customer is
 * only ever talking about the job in front of them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTaskDetailScreen(
  task: TaskEntity,
  messages: List<ChatMessageEntity>,
  onBack: () -> Unit,
  onSend: (String) -> Unit,
  onCancelTask: () -> Unit,
) {
  val colors = FixTheme.colors
  var draft by remember { mutableStateOf("") }
  var confirmingCancel by remember { mutableStateOf(false) }
  val chatOpen = hasAssignedHelper(task.status)

  if (confirmingCancel) {
    AlertDialog(
      onDismissRequest = { confirmingCancel = false },
      containerColor = colors.surface,
      titleContentColor = colors.textPrimary,
      textContentColor = colors.textSecondary,
      title = { Text("Cancel this task?", fontWeight = FontWeight.SemiBold) },
      text = {
        Text(
          "It stops being visible to helpers. This cannot be undone — you would have to post it " +
            "again."
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            confirmingCancel = false
            onCancelTask()
          }
        ) {
          Text("Cancel task", color = colors.danger, fontWeight = FontWeight.SemiBold)
        }
      },
      dismissButton = {
        TextButton(onClick = { confirmingCancel = false }) {
          Text("Keep it", color = colors.textSecondary)
        }
      },
    )
  }

  Scaffold(
    containerColor = colors.background,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = task.descriptionTitle.ifBlank { "Untitled task" },
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to my tasks")
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = colors.surface,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.textPrimary,
          ),
      )
    },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {
      TaskSummary(
        task = task,
        onRequestCancel = { confirmingCancel = true },
        modifier = Modifier.padding(Spacing.lg),
      )

      if (!chatOpen) {
        // No thread before a helper is assigned: there is nobody on the other end, and an empty
        // input would imply otherwise.
        Box(modifier = Modifier.fillMaxSize().padding(Spacing.xl), contentAlignment = Alignment.TopCenter) {
          Text(
            text = "Messaging opens once a helper takes this task on.",
            color = colors.textSecondary,
          )
        }
        return@Column
      }

      if (messages.isEmpty()) {
        Box(
          modifier = Modifier.weight(1f).fillMaxWidth().padding(Spacing.xl),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "No messages yet. Say hello to your helper.",
            color = colors.textSecondary,
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          // Newest first, matching the DAO's ORDER BY timestamp DESC.
          reverseLayout = true,
          contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
        ) {
          items(messages, key = { it.id }) { message ->
            MessageBubble(
              text = message.text,
              isSender = message.senderId == ChatRepository.SENDER_CUSTOMER,
              time = relativeTimeText(message.timestamp),
            )
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = draft,
          onValueChange = { draft = it },
          modifier = Modifier.weight(1f),
          placeholder = { Text("Message your helper") },
          shape = RoundedCornerShape(Radius.md),
          maxLines = 4,
        )
        Spacer(Modifier.size(Spacing.sm))
        IconButton(
          onClick = {
            onSend(draft)
            draft = ""
          },
          enabled = draft.isNotBlank(),
        ) {
          Icon(
            Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send message",
            tint = if (draft.isNotBlank()) colors.primary else colors.disabled,
          )
        }
      }
    }
  }
}

@Composable
private fun TaskSummary(task: TaskEntity, onRequestCancel: () -> Unit, modifier: Modifier = Modifier) {
  val colors = FixTheme.colors
  FixCard(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(Spacing.lg)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        StatusBadge(text = customerStatusLabel(task.status), tone = task.status.customerTone())
        Text(
          text = relativeTimeText(task.createdAt),
          fontSize = 12.sp,
          color = colors.textSecondary,
        )
      }

      Spacer(Modifier.height(Spacing.sm))
      Text(text = customerStatusDetail(task.status), color = colors.textSecondary)

      if (task.descriptionDetails.isNotBlank()) {
        Spacer(Modifier.height(Spacing.md))
        Text(text = task.descriptionDetails, color = colors.textPrimary)
      }

      Spacer(Modifier.height(Spacing.md))
      Text(
        text = taskLocationText(task.locationQuery, task.latitude != null),
        color = colors.textSecondary,
      )
      Text(text = budgetText(task.minBudget, task.maxBudget), color = colors.textSecondary)

      if (isCancellableByCustomer(task.status)) {
        Spacer(Modifier.height(Spacing.lg))
        FixButton(
          text = "Cancel this task",
          onClick = onRequestCancel,
          style = FixButtonStyle.DANGER,
        )
      }
    }
  }
}
