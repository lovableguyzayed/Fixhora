package com.example.ui.screens.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusTone
import com.example.ui.screens.taskflow.dummyCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerTasksScreen(viewModel: HelperViewModel) {
    val allTasks by viewModel.allTasks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    // "Pending" was dropped: no task ever carried that status, so the tab was always empty.
    val tabs = listOf("All Tasks", "New Requests", "Accepted", "In Progress", "Completed", "Cancelled")
    var selectedTab by remember { mutableStateOf("All Tasks") }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(FixTheme.colors.surfaceAlt),
        topBar = {
            TopAppBar(
                title = { Text("My Tasks", fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary) },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = FixTheme.colors.textPrimary)
                    }
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = FixTheme.colors.textPrimary)
                    }
                    IconButton(onClick = { /* Sort */ }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort", tint = FixTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.surface)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(FixTheme.colors.surfaceAlt)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tasks, customers, locations...", color = FixTheme.colors.textSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FixTheme.colors.textSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = FixTheme.colors.border,
                    focusedBorderColor = FixTheme.colors.primary,
                    unfocusedContainerColor = FixTheme.colors.surface,
                    focusedContainerColor = FixTheme.colors.surface
                ),
                singleLine = true
            )

            // Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = {
                            Text(
                                text = if (tab == "All Tasks") "$tab (${allTasks.size})" else tab,
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

            // Task List
            val filteredTasks = if (selectedTab == "All Tasks") allTasks else allTasks.filter {
                when (selectedTab) {
                    "New Requests" -> it.status == TaskStatus.SUBMITTED
                    "Accepted" -> it.status == TaskStatus.ACCEPTED
                    "In Progress" -> it.status == TaskStatus.IN_PROGRESS
                    "Completed" -> it.status == TaskStatus.COMPLETED
                    "Cancelled" -> it.status == TaskStatus.CANCELLED
                    else -> false
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = if (selectedTab == "All Tasks") {
                            "No tasks yet"
                        } else {
                            "Nothing in \"$selectedTab\""
                        },
                        description = if (selectedTab == "All Tasks") {
                            "New customer requests will appear here as soon as they are posted."
                        } else {
                            "Tasks move here as their status changes."
                        }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks) { task ->
                        WorkerTaskCard(
                            task = task,
                            onAccept = { viewModel.acceptTask(task) },
                            onReject = { viewModel.rejectTask(task) }
                        )
                    }
                }
            }
        }
    }
}

// Map TaskEntity to display format
@Composable
fun WorkerTaskCard(task: TaskEntity, onAccept: () -> Unit, onReject: () -> Unit) {
    val category = dummyCategories.find { it.id == task.categoryId }?.title ?: "General Service"
    val budgetText = if (task.minBudget.isNotBlank() && task.maxBudget.isNotBlank()) "₹${task.minBudget} - ₹${task.maxBudget}" else if (task.minBudget.isNotBlank()) "Min ₹${task.minBudget}" else if (task.maxBudget.isNotBlank()) "Max ₹${task.maxBudget}" else "Budget Negotiable"
    val displayStatus = when (task.status) {
        TaskStatus.SUBMITTED -> "New"
        TaskStatus.ACCEPTED -> "Accepted"
        TaskStatus.IN_PROGRESS -> "In Progress"
        TaskStatus.COMPLETED -> "Completed"
        TaskStatus.CANCELLED -> "Cancelled"
        TaskStatus.REJECTED -> "Rejected"
        TaskStatus.DRAFT -> "Draft"
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* View Details */ },
        colors = CardDefaults.cardColors(containerColor = FixTheme.colors.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Customer Info & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(FixTheme.colors.border),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = FixTheme.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Customer Request", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FixTheme.colors.textPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = FixTheme.colors.accentGraphic, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New User", fontSize = 12.sp, color = FixTheme.colors.textSecondary, fontWeight = FontWeight.Medium)
                            Text(" • Just now", fontSize = 12.sp, color = FixTheme.colors.textSecondary)
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(
                        text = displayStatus,
                        tone = when (task.status) {
                            TaskStatus.SUBMITTED -> StatusTone.SUCCESS
                            TaskStatus.ACCEPTED -> StatusTone.INFO
                            TaskStatus.IN_PROGRESS -> StatusTone.WARNING
                            TaskStatus.COMPLETED -> StatusTone.SUCCESS
                            TaskStatus.CANCELLED, TaskStatus.REJECTED -> StatusTone.DANGER
                            TaskStatus.DRAFT -> StatusTone.NEUTRAL
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Job Details
            Text(task.descriptionTitle.ifEmpty { "Need Help with $category" }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FixTheme.colors.textPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                task.descriptionDetails.ifEmpty { "Looking for someone to help me out with this task." },
                fontSize = 14.sp,
                color = FixTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Job Metadata Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    TaskMetaRow(Icons.Default.Category, category)
                    Spacer(modifier = Modifier.height(4.dp))
                    TaskMetaRow(Icons.Default.LocationOn, if (task.useCurrentLocation) "Current Location" else task.locationQuery.ifEmpty { "Not specified" })
                }
                Column(modifier = Modifier.weight(1f)) {
                    TaskMetaRow(Icons.Default.AccountBalanceWallet, budgetText)
                    Spacer(modifier = Modifier.height(4.dp))
                    TaskMetaRow(Icons.Default.Schedule, "ASAP")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = FixTheme.colors.border)
            Spacer(modifier = Modifier.height(12.dp))

            // Actions based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (displayStatus == "New") {
                    Text("0 bids", fontSize = 12.sp, color = FixTheme.colors.textSecondary, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FixTheme.colors.danger),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onAccept,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FixTheme.colors.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (displayStatus == "Accepted") {
                    TextButton(onClick = { /* Chat */ }) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chat")
                    }
                    Button(
                        onClick = { /* Navigate / Start */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FixTheme.colors.primary)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Navigate", fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { /* Details */ }) {
                        Text("View Details")
                    }
                    Button(
                        onClick = { /* Place Bid */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FixTheme.colors.primary)
                    ) {
                        Text("Place Bid", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskMetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = FixTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = FixTheme.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
