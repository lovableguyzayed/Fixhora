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
import com.example.ui.format.budgetLabel
import com.example.ui.format.locationLabel
import com.example.ui.format.posterLabel
import com.example.ui.format.relativeTimeLabel
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusTone
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerTasksScreen(viewModel: HelperViewModel, onOpenChat: (Int) -> Unit) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val query by viewModel.taskQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val ownerNames by viewModel.ownerNames.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My tasks", fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary) },
                // The Search / Filter / Sort icons are gone. Search is the field below, and
                // neither filter nor sort was ever implemented.
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.surface)
            )
        },
        containerColor = FixTheme.colors.surfaceAlt
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onTaskQueryChange,
                placeholder = { Text("Search by title, address or category") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FixTheme.colors.textSecondary) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onTaskQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = FixTheme.colors.border,
                    focusedBorderColor = FixTheme.colors.primary,
                    unfocusedContainerColor = FixTheme.colors.surface,
                    focusedContainerColor = FixTheme.colors.surface
                )
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TaskTab.entries.toList()) { tab ->
                    val isSelected = selectedTab == tab
                    val count =
                        if (tab.status == null) allTasks.size else allTasks.count { it.status == tab.status }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onTabSelected(tab) },
                        label = {
                            Text(
                                text = "${stringResource(tab.labelRes)} ($count)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FixTheme.colors.primary,
                            selectedLabelColor = FixTheme.colors.onPrimary,
                            containerColor = FixTheme.colors.surface,
                            labelColor = FixTheme.colors.textPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) FixTheme.colors.primary else FixTheme.colors.border
                        )
                    )
                }
            }

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = when {
                            query.isNotBlank() -> "Nothing matches \"$query\""
                            selectedTab == TaskTab.ALL -> "No tasks yet"
                            else -> "Nothing in \"${stringResource(selectedTab.labelRes)}\""
                        },
                        description = when {
                            query.isNotBlank() -> "Try a different word, or clear the search."
                            selectedTab == TaskTab.ALL ->
                                "Customer requests appear here as soon as they are posted."
                            else -> "Tasks move here as their status changes."
                        },
                        actionText = if (query.isNotBlank()) "Clear search" else null,
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
                            posterName = posterLabel(task.ownerId, ownerNames),
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
                "geo:${task.latitude},${task.longitude}?q=${Uri.encode(task.locationQuery.ifBlank { "Task location" })}"
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
    val category = dummyCategories.find { it.id == task.categoryId }
    val postedAgo = relativeTimeLabel(task.createdAt, System.currentTimeMillis())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FixTheme.colors.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        posterName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FixTheme.colors.textPrimary
                    )
                    Text(postedAgo, fontSize = 12.sp, color = FixTheme.colors.textSecondary)
                }
                StatusBadge(text = task.status.displayLabel(), tone = task.status.tone())
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                task.descriptionTitle.ifBlank { category?.let { stringResource(it.titleRes) } ?: stringResource(R.string.task_untitled) },
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = FixTheme.colors.textPrimary
            )
            if (task.descriptionDetails.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    task.descriptionDetails,
                    fontSize = 14.sp,
                    color = FixTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    TaskMetaRow(Icons.Default.Category, category?.let { stringResource(it.titleRes) } ?: stringResource(R.string.task_uncategorised))
                    Spacer(modifier = Modifier.height(4.dp))
                    TaskMetaRow(
                        Icons.Default.LocationOn,
                        locationLabel(task.locationQuery, task.latitude != null)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    TaskMetaRow(
                        Icons.Default.AccountBalanceWallet,
                        budgetLabel(task.minBudget, task.maxBudget)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TaskMetaRow(Icons.Default.MyLocation, "Within ${task.selectedDistance} km")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = FixTheme.colors.border)
            Spacer(modifier = Modifier.height(12.dp))

            // Each state offers only actions that actually do something in this build.
            when (task.status) {
                TaskStatus.SUBMITTED ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDecline,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FixTheme.colors.textSecondary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FixTheme.colors.border),
                            modifier = Modifier.weight(1f)
                        ) { Text("Decline", fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = onAccept,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FixTheme.colors.primary,
                                contentColor = FixTheme.colors.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Accept", fontWeight = FontWeight.Bold) }
                    }

                TaskStatus.ACCEPTED ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardAction(Icons.AutoMirrored.Filled.Chat, "Chat", onChat, Modifier.weight(1f))
                        CardAction(Icons.Default.Navigation, "Directions", onNavigate, Modifier.weight(1f))
                        Button(
                            onClick = onStart,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FixTheme.colors.primary,
                                contentColor = FixTheme.colors.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Start", fontWeight = FontWeight.Bold) }
                    }

                TaskStatus.IN_PROGRESS ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CardAction(Icons.AutoMirrored.Filled.Chat, "Chat", onChat, Modifier.weight(1f))
                        CardAction(Icons.Default.Navigation, "Directions", onNavigate, Modifier.weight(1f))
                        Button(
                            onClick = onComplete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FixTheme.colors.success,
                                contentColor = FixTheme.colors.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Done", fontWeight = FontWeight.Bold) }
                    }

                TaskStatus.COMPLETED ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FixTheme.colors.success,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Finished",
                            color = FixTheme.colors.success,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        CardAction(Icons.AutoMirrored.Filled.Chat, "Chat", onChat)
                    }

                else ->
                    Text(
                        "No actions available for this task.",
                        fontSize = 13.sp,
                        color = FixTheme.colors.textSecondary
                    )
            }
        }
    }
}

@Composable
private fun CardAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = FixTheme.colors.primary)
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = FixTheme.colors.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
fun TaskMetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = FixTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text,
            fontSize = 12.sp,
            color = FixTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun TaskStatus.displayLabel(): String =
    when (this) {
        TaskStatus.DRAFT -> "Draft"
        TaskStatus.SUBMITTED -> "New"
        TaskStatus.ACCEPTED -> "Accepted"
        TaskStatus.IN_PROGRESS -> "In progress"
        TaskStatus.COMPLETED -> "Completed"
        TaskStatus.CANCELLED -> "Cancelled"
        TaskStatus.REJECTED -> "Declined"
    }

fun TaskStatus.tone(): StatusTone =
    when (this) {
        TaskStatus.SUBMITTED -> StatusTone.INFO
        TaskStatus.ACCEPTED -> StatusTone.WARNING
        TaskStatus.IN_PROGRESS -> StatusTone.WARNING
        TaskStatus.COMPLETED -> StatusTone.SUCCESS
        TaskStatus.CANCELLED, TaskStatus.REJECTED -> StatusTone.DANGER
        TaskStatus.DRAFT -> StatusTone.NEUTRAL
    }
