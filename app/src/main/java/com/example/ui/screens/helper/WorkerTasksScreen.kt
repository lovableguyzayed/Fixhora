package com.example.ui.screens.helper

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CircleBackButton
import com.example.ui.format.budgetText
import com.example.ui.format.taskLocationText
import com.example.ui.format.posterText
import com.example.ui.format.relativeTimeText
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.ui.components.BentoAction
import com.example.ui.components.BentoActionRow
import com.example.ui.components.EmptyState
import com.example.ui.components.PillChip
import com.example.ui.components.SearchField
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusTone
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerTasksScreen(viewModel: HelperViewModel, onOpenChat: (Int) -> Unit, onBack: () -> Unit = {}) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val query by viewModel.taskQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val ownerNames by viewModel.ownerNames.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    CircleBackButton(
                        onClick = onBack,
                        contentDescription = stringResource(R.string.cd_back),
                        modifier = Modifier.padding(start = Spacing.xs)
                    )
                },
                title = { Text(stringResource(R.string.worker_tasks_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = FixTheme.colors.textPrimary) },
                // The Search / Filter / Sort icons are gone. Search is the field below, and
                // neither filter nor sort was ever implemented.
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.background)
            )
        },
        containerColor = FixTheme.colors.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            SearchField(
                query = query,
                onQueryChange = viewModel::onTaskQueryChange,
                placeholder = stringResource(R.string.worker_tasks_search_hint),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TaskTab.entries.toList()) { tab ->
                    val isSelected = selectedTab == tab
                    val count =
                        if (tab.status == null) allTasks.size else allTasks.count { it.status == tab.status }
                    PillChip(
                        text = stringResource(R.string.worker_tab_with_count, stringResource(tab.labelRes), count),
                        selected = isSelected,
                        onClick = { viewModel.onTabSelected(tab) }
                    )
                }
            }

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = when {
                            query.isNotBlank() -> stringResource(R.string.worker_tasks_empty_query_title, query)
                            selectedTab == TaskTab.ALL -> stringResource(R.string.worker_tasks_empty_all_title)
                            else ->
                                stringResource(
                                    R.string.worker_tasks_empty_tab_title,
                                    stringResource(selectedTab.labelRes)
                                )
                        },
                        description = when {
                            query.isNotBlank() -> stringResource(R.string.worker_tasks_empty_query_body)
                            selectedTab == TaskTab.ALL ->
                                stringResource(R.string.worker_tasks_empty_all_body)
                            else -> stringResource(R.string.worker_tasks_empty_tab_body)
                        },
                        actionText = if (query.isNotBlank()) stringResource(R.string.action_clear_search) else null,
                        onAction = if (query.isNotBlank()) {
                            { viewModel.onTaskQueryChange("") }
                        } else null
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tasks, key = { it.id }) { task ->
                        WorkerTaskCard(
                            task = task,
                            posterName = posterText(task.ownerId, ownerNames),
                            onAccept = { viewModel.acceptTask(task) },
                            onDecline = { viewModel.rejectTask(task) },
                            onStart = { viewModel.startTask(task) },
                            onComplete = { viewModel.completeTask(task) },
                            onChat = { onOpenChat(task.id) },
                            onNavigate = { context.openDirectionsTo(task) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hands the address to whatever maps app the device has.
 *
 * The "Navigate" button used to do nothing. A `geo:` intent needs no API key and works with any
 * installed maps app; if there is none, the tap is a no-op rather than a crash.
 */
private fun android.content.Context.openDirectionsTo(task: TaskEntity) {
    val destination =
        when {
            task.latitude != null && task.longitude != null ->
                "geo:${task.latitude},${task.longitude}?q=${Uri.encode(task.locationQuery.ifBlank { getString(R.string.map_query_fallback) })}"
            task.locationQuery.isNotBlank() -> "geo:0,0?q=${Uri.encode(task.locationQuery)}"
            else -> return
        }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(destination)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (intent.resolveActivity(packageManager) != null) startActivity(intent)
}

@Composable
fun WorkerTaskCard(
    task: TaskEntity,
    posterName: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onChat: () -> Unit,
    onNavigate: () -> Unit
) {
    // Chat exists from acceptance onwards; before that there is nobody to talk to yet.
    val canChat = task.status == TaskStatus.ACCEPTED ||
        task.status == TaskStatus.IN_PROGRESS ||
        task.status == TaskStatus.COMPLETED
    JobBentoCard(
        task = task,
        posterName = posterName,
        statusLabel = task.status.displayLabel(),
        onChat = if (canChat) onChat else null,
        chatLabel = stringResource(R.string.action_chat)
    ) { bento ->
        // Each state offers only actions that actually do something in this build.
        when (task.status) {
            TaskStatus.SUBMITTED -> BentoActionRow {
                BentoAction(stringResource(R.string.action_decline), onDecline, bento, filled = false)
                BentoAction(stringResource(R.string.action_accept), onAccept, bento, filled = true)
            }
            TaskStatus.ACCEPTED -> BentoActionRow {
                BentoAction(stringResource(R.string.action_directions), onNavigate, bento, filled = false, icon = Icons.Default.Navigation)
                BentoAction(stringResource(R.string.action_start), onStart, bento, filled = true)
            }
            TaskStatus.IN_PROGRESS -> BentoActionRow {
                BentoAction(stringResource(R.string.action_directions), onNavigate, bento, filled = false, icon = Icons.Default.Navigation)
                BentoAction(stringResource(R.string.action_mark_done), onComplete, bento, filled = true, icon = Icons.Default.Check)
            }
            TaskStatus.COMPLETED -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = bento.onFill,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.worker_finished),
                    color = bento.onFill,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            else -> Text(
                stringResource(R.string.worker_no_actions),
                style = MaterialTheme.typography.bodySmall,
                color = bento.onFillSecondary
            )
        }
    }
}

/**
 * What a status is called **to the worker**, which is not what it is called to the customer:
 * `REJECTED` reads as "Declined" here because the worker is the one who declined it, while
 * [com.example.ui.format.customerStatusLabel] renders the same row as "No helper yet".
 */
@Composable
fun TaskStatus.displayLabel(): String =
    stringResource(
        when (this) {
            TaskStatus.DRAFT -> R.string.worker_status_draft
            TaskStatus.SUBMITTED -> R.string.worker_status_new
            TaskStatus.ACCEPTED -> R.string.worker_status_accepted
            TaskStatus.IN_PROGRESS -> R.string.worker_status_in_progress
            TaskStatus.COMPLETED -> R.string.worker_status_completed
            TaskStatus.CANCELLED -> R.string.worker_status_cancelled
            TaskStatus.REJECTED -> R.string.worker_status_declined
        }
    )

fun TaskStatus.tone(): StatusTone =
    when (this) {
        TaskStatus.SUBMITTED -> StatusTone.INFO
        TaskStatus.ACCEPTED -> StatusTone.WARNING
        TaskStatus.IN_PROGRESS -> StatusTone.WARNING
        TaskStatus.COMPLETED -> StatusTone.SUCCESS
        TaskStatus.CANCELLED, TaskStatus.REJECTED -> StatusTone.DANGER
        TaskStatus.DRAFT -> StatusTone.NEUTRAL
    }
