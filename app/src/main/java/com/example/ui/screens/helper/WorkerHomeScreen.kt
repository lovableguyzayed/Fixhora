package com.example.ui.screens.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.components.EmptyState
import com.example.ui.components.SectionHeader
import com.example.ui.components.SkeletonBox
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusTone
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHomeScreen(viewModel: HelperViewModel, onOpenChat: (Int) -> Unit) {
    val tasks by viewModel.availableTasks.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val ownerNames by viewModel.ownerNames.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(600)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Find work nearby",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FixTheme.colors.textPrimary
                        )
                        Text(
                            text = if (stats.activeNow > 0) {
                                "${stats.activeNow} job${if (stats.activeNow == 1) "" else "s"} in progress"
                            } else {
                                "No jobs in progress"
                            },
                            fontSize = 12.sp,
                            color = FixTheme.colors.textSecondary
                        )
                    }
                },
                // The greeting with a hardcoded "Alex Worker", the online/offline pill that changed
                // nothing, and the notification bell with a "2" badge are all gone: none of them
                // was connected to anything.
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.surface)
            )
        },
        containerColor = FixTheme.colors.surfaceAlt
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { WorkSummaryCard(stats) }

            item {
                SectionHeader(title = "Your pipeline")
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        SummaryCard("Open nearby", stats.availableNow, Icons.Default.Search,
                            FixTheme.colors.infoSurface, FixTheme.colors.info)
                    }
                    item {
                        SummaryCard("Accepted", stats.accepted, Icons.AutoMirrored.Filled.Assignment,
                            FixTheme.colors.warningSurface, FixTheme.colors.warning)
                    }
                    item {
                        SummaryCard("In progress", stats.inProgress, Icons.Default.PendingActions,
                            FixTheme.colors.warningSurface, FixTheme.colors.warning)
                    }
                    item {
                        SummaryCard("Completed", stats.completed, Icons.Default.CheckCircle,
                            FixTheme.colors.successSurface, FixTheme.colors.success)
                    }
                    item {
                        SummaryCard("Declined", stats.declined, Icons.Default.Cancel,
                            FixTheme.colors.dangerSurface, FixTheme.colors.danger)
                    }
                }
            }

            item { SectionHeader(title = "Available nearby jobs") }

            if (isLoading) {
                items(3) { JobCardSkeleton() }
            } else if (tasks.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "No jobs nearby right now",
                        description = "New requests from customers appear here as soon as they are posted.",
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    WorkerJobCard(
                        task = task,
                        posterName = posterLabel(task.ownerId, ownerNames),
                        onAccept = { viewModel.acceptTask(task) },
                        onDecline = { viewModel.rejectTask(task) },
                        onMessage = { onOpenChat(task.id) }
                    )
                }
            }
        }
    }
}

/**
 * The three numbers the app can actually prove.
 *
 * This replaces a "Performance Overview" card of earnings, a star rating, a response time and
 * three percentage rings, none of which had any data behind them. Payments, ratings and response
 * times are not tracked, so they are not shown.
 */
@Composable
fun WorkSummaryCard(stats: HelperStats) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FixTheme.colors.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Your work",
                color = FixTheme.colors.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeadlineStat("Open nearby", stats.availableNow)
                HeadlineStat("Active", stats.activeNow)
                HeadlineStat("Completed", stats.completed)
            }
        }
    }
}

@Composable
private fun HeadlineStat(label: String, value: Int) {
    Column {
        Text(
            text = value.toString(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = FixTheme.colors.onPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = FixTheme.colors.onPrimary.copy(alpha = 0.75f)
        )
    }
}

@Composable
fun SummaryCard(title: String, count: Int, icon: ImageVector, bgColor: Color, iconColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.width(120.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = FixTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 12.sp, color = FixTheme.colors.textSecondary)
        }
    }
}

@Composable
fun JobCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FixTheme.colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                SkeletonBox(modifier = Modifier.size(40.dp), height = 40.dp, cornerRadius = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    SkeletonBox(modifier = Modifier.width(120.dp), height = 16.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonBox(modifier = Modifier.width(80.dp), height = 12.dp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.8f), height = 20.dp)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 14.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBox(modifier = Modifier.weight(1f), height = 48.dp, cornerRadius = 12.dp)
                SkeletonBox(modifier = Modifier.weight(1f), height = 48.dp, cornerRadius = 12.dp)
            }
        }
    }
}

@Composable
fun WorkerJobCard(
    task: TaskEntity,
    posterName: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onMessage: () -> Unit
) {
    val category = dummyCategories.find { it.id == task.categoryId }
    // Recomputed per composition rather than captured once, so the age does not freeze on screen.
    val postedAgo = relativeTimeLabel(task.createdAt, System.currentTimeMillis())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FixTheme.colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                            .background(FixTheme.colors.surfaceAlt),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = FixTheme.colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        // Was "Customer Name" with a verified tick and a 4.8 rating. Nothing
                        // verifies anyone and there is no rating system, so neither is shown.
                        Text(
                            posterName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FixTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(postedAgo, fontSize = 12.sp, color = FixTheme.colors.textSecondary)
                    }
                }
                StatusBadge(text = "Open", tone = StatusTone.INFO)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = task.descriptionTitle.ifBlank { category?.let { stringResource(it.titleRes) } ?: stringResource(R.string.task_untitled) },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = FixTheme.colors.textPrimary
            )
            if (task.descriptionDetails.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.descriptionDetails,
                    color = FixTheme.colors.textSecondary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            JobMetaRow(
                Icons.Default.LocationOn,
                locationLabel(task.locationQuery, task.latitude != null)
            )
            Spacer(modifier = Modifier.height(6.dp))
            JobMetaRow(Icons.Default.MyLocation, "Within ${task.selectedDistance} km")
            Spacer(modifier = Modifier.height(6.dp))
            JobMetaRow(
                Icons.Default.AccountBalanceWallet,
                budgetLabel(task.minBudget, task.maxBudget),
                emphasise = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = FixTheme.colors.border)
            Spacer(modifier = Modifier.height(16.dp))

            // "Place Bid" is gone: there is no bidding system for it to submit anything to.
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FixTheme.colors.textSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FixTheme.colors.border)
                ) {
                    Text("Not for me", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FixTheme.colors.primary,
                        contentColor = FixTheme.colors.onPrimary
                    )
                ) {
                    Text("Accept job", fontWeight = FontWeight.Bold)
                }
            }
            TextButton(
                onClick = onMessage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = FixTheme.colors.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ask a question", color = FixTheme.colors.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun JobMetaRow(icon: ImageVector, text: String, emphasise: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (emphasise) FixTheme.colors.textPrimary else FixTheme.colors.textSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (emphasise) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasise) FixTheme.colors.textPrimary else FixTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
