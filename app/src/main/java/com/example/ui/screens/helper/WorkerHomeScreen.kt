package com.example.ui.screens.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.room.TaskEntity
import com.example.ui.components.BentoAction
import com.example.ui.components.BentoActionRow
import com.example.ui.components.BentoStat
import com.example.ui.components.EmptyState
import com.example.ui.components.FixTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.SkeletonBox
import com.example.ui.format.posterText
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHomeScreen(viewModel: HelperViewModel, onOpenChat: (Int) -> Unit, onBack: () -> Unit = {}) {
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
            // The greeting with a hardcoded "Alex Worker", the online/offline pill that changed
            // nothing, and the notification bell with a "2" badge are all gone: none of them was
            // connected to anything.
            FixTopBar(title = stringResource(R.string.worker_home_title), onBack = onBack)
        },
        containerColor = FixTheme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, top = Spacing.sm, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            item { WorkSummaryGrid(stats) }

            item { SectionHeader(title = stringResource(R.string.worker_available_jobs)) }

            if (isLoading) {
                items(3) { JobCardSkeleton() }
            } else if (tasks.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = stringResource(R.string.worker_empty_title),
                        description = stringResource(R.string.worker_empty_body),
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    WorkerJobCard(
                        task = task,
                        posterName = posterText(task.ownerId, ownerNames),
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
 * The numbers the app can actually prove, as coloured tiles: one large figure for the jobs open
 * now, and the four stages of the helper's own work beneath it.
 *
 * This replaces a "Performance Overview" card of earnings, a star rating, a response time and
 * three percentage rings, none of which had any data behind them. Payments, ratings and response
 * times are not tracked, so they are not shown.
 */
@Composable
fun WorkSummaryGrid(stats: HelperStats) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        BentoStat(
            value = stats.availableNow.toString(),
            label = stringResource(R.string.worker_stat_open_nearby),
            colors = Bento.Tangerine,
            icon = Icons.Default.NearMe,
            caption = if (stats.activeNow > 0) {
                pluralStringResource(R.plurals.jobs_in_progress, stats.activeNow, stats.activeNow)
            } else {
                stringResource(R.string.worker_no_jobs_in_progress)
            },
            large = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            BentoStat(
                value = stats.accepted.toString(),
                label = stringResource(R.string.worker_stat_accepted),
                colors = Bento.Sun,
                icon = Icons.AutoMirrored.Filled.Assignment,
                modifier = Modifier.weight(1f)
            )
            BentoStat(
                value = stats.inProgress.toString(),
                label = stringResource(R.string.worker_stat_in_progress),
                colors = Bento.Periwinkle,
                icon = Icons.Default.PendingActions,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            BentoStat(
                value = stats.completed.toString(),
                label = stringResource(R.string.worker_stat_completed),
                colors = Bento.Lime,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
            BentoStat(
                value = stats.declined.toString(),
                label = stringResource(R.string.worker_stat_declined),
                colors = Bento.Peach,
                icon = Icons.Default.Cancel,
                modifier = Modifier.weight(1f)
            )
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
    JobBentoCard(
        task = task,
        posterName = posterName,
        statusLabel = stringResource(R.string.job_status_open),
        onChat = onMessage,
        chatLabel = stringResource(R.string.action_ask_question)
    ) { bento ->
        // "Place Bid" is gone: there is no bidding system for it to submit anything to.
        BentoActionRow {
            BentoAction(stringResource(R.string.action_not_for_me), onDecline, bento, filled = false)
            BentoAction(stringResource(R.string.action_accept_job), onAccept, bento, filled = true)
        }
    }
}
