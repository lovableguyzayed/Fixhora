package com.example.ui.screens.helper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.room.TaskEntity
import com.example.ui.components.CompactButton
import com.example.ui.components.EmptyState
import com.example.ui.components.FixhoraCard
import com.example.ui.components.FixhoraTextField
import com.example.ui.components.MetaItem
import com.example.ui.components.MockPeople
import com.example.ui.components.Spacing
import com.example.ui.components.StatusChip
import com.example.ui.components.areaLabel
import com.example.ui.components.budgetLabel
import com.example.ui.components.relativeTime
import com.example.ui.screens.taskflow.categoryFor
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*

private enum class TaskTab(val label: String, val status: String?) {
    All("All", null),
    New("New", "submitted"),
    BidSent("Bid sent", "pending"),
    Active("Active", "accepted"),
    Completed("Completed", "completed"),
    Declined("Declined", "rejected"),
}

@Composable
fun WorkerTasksScreen(
    viewModel: HelperViewModel,
    onOpenChat: (Int) -> Unit,
    onExploreJobs: () -> Unit,
) {
    val allTasks by viewModel.allTasks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(TaskTab.All) }

    val filteredTasks = allTasks.filter { task ->
        (selectedTab.status == null || task.status == selectedTab.status) &&
            (searchQuery.isBlank() ||
                task.descriptionTitle.contains(searchQuery, ignoreCase = true) ||
                MockPeople.customerName(task.id).contains(searchQuery, ignoreCase = true) ||
                task.areaLabel().contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = { HelperTopBar(title = { Text("My tasks", style = MaterialTheme.typography.titleLarge, color = DarkNavy) }) },
        containerColor = MutedBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Surface(color = Color.White) {
                Column {
                    FixhoraTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Search tasks, customers or areas",
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier.padding(horizontal = Spacing.md),
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(20.dp))
                                }
                            }
                        } else null
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        items(TaskTab.entries.toList()) { tab ->
                            val isSelected = selectedTab == tab
                            val count = if (tab.status == null) allTasks.size else allTasks.count { it.status == tab.status }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                label = {
                                    Text(
                                        text = "${tab.label} ($count)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BluePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = DarkNavy
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = BorderGrey,
                                    selectedBorderColor = BluePrimary
                                )
                            )
                        }
                    }
                    HorizontalDivider(color = DividerGrey)
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = if (searchQuery.isBlank()) "Nothing here yet" else "No tasks found",
                        message = if (searchQuery.isBlank()) {
                            "Tasks you accept, bid on or complete will show up here."
                        } else {
                            "Try a different search or tab."
                        },
                        actionLabel = if (searchQuery.isBlank()) "Explore nearby jobs" else null,
                        onAction = onExploreJobs
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        WorkerTaskCard(
                            task = task,
                            onAccept = { viewModel.acceptTask(task) },
                            onReject = { viewModel.rejectTask(task) },
                            onComplete = { viewModel.completeTask(task) },
                            onReopen = { viewModel.reopenTask(task) },
                            onChat = { onOpenChat(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkerTaskCard(
    task: TaskEntity,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onReopen: () -> Unit,
    onChat: () -> Unit,
) {
    val category = categoryFor(task.categoryId) ?: dummyCategories.last()
    val status = statusStyle(task.status)

    FixhoraCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            CustomerRow(task = task, customer = MockPeople.customerName(task.id)) {
                StatusChip(text = status.label, color = status.color, showDot = true)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                task.descriptionTitle.ifBlank { "Help with ${category.title.lowercase()}" },
                style = MaterialTheme.typography.titleMedium,
                color = DarkNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(
                task.descriptionDetails.ifBlank { "No extra details added." },
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryGrey,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    MetaItem(category.icon, category.title)
                    MetaItem(Icons.Default.LocationOn, task.areaLabel())
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    MetaItem(Icons.Default.AccountBalanceWallet, task.budgetLabel(), color = DarkNavy)
                    MetaItem(Icons.Default.AccessTime, relativeTime(task.createdAt).let { if (it == "Just now") "Posted just now" else "Posted $it" })
                }
            }

            HorizontalDivider(color = DividerGrey, modifier = Modifier.padding(vertical = Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (task.status) {
                    "submitted" -> {
                        CompactButton(text = "Decline", onClick = onReject, outlined = true, color = ErrorRed)
                        CompactButton(text = "Accept", onClick = onAccept)
                    }
                    "pending" -> {
                        CompactButton(text = "Withdraw", onClick = onReopen, outlined = true, color = SecondaryGrey)
                        CompactButton(text = "Chat", onClick = onChat, leadingIcon = Icons.AutoMirrored.Filled.Chat)
                    }
                    "accepted" -> {
                        CompactButton(text = "Chat", onClick = onChat, outlined = true, leadingIcon = Icons.AutoMirrored.Filled.Chat)
                        CompactButton(text = "Mark complete", onClick = onComplete, color = SuccessGreen, leadingIcon = Icons.Default.CheckCircle)
                    }
                    "rejected" -> {
                        CompactButton(text = "Reconsider", onClick = onReopen, outlined = true, leadingIcon = Icons.AutoMirrored.Filled.Undo)
                    }
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Job completed", style = MaterialTheme.typography.titleSmall, color = SuccessGreen)
                        }
                        CompactButton(text = "Chat", onClick = onChat, outlined = true, leadingIcon = Icons.AutoMirrored.Filled.Chat)
                    }
                }
            }
        }
    }
}
