package com.example.ui.screens.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyState
import com.example.ui.components.FixhoraCard
import com.example.ui.components.IconBadge
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.MockPeople
import com.example.ui.components.SectionTitle
import com.example.ui.components.Spacing
import com.example.ui.components.StatusChip
import com.example.ui.components.greetingForNow
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WorkerHomeScreen(
    viewModel: HelperViewModel,
    onSwitchRole: () -> Unit,
    onOpenChat: (Int) -> Unit,
    onSeeAllTasks: () -> Unit,
) {
    val tasks by viewModel.availableTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    var isOnline by rememberSaveableBoolean(true)
    var isLoading by remember { mutableStateOf(true) }
    var sheet by remember { mutableStateOf<JobSheet?>(null) }
    var showProfileMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800)
        isLoading = false
    }

    Scaffold(
        topBar = {
            HelperTopBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Surface(onClick = { showProfileMenu = true }, shape = CircleShape, color = Color.Transparent) {
                                InitialsAvatar(name = MockPeople.helperName, size = 40.dp)
                            }
                            DropdownMenu(
                                expanded = showProfileMenu,
                                onDismissRequest = { showProfileMenu = false },
                                containerColor = Color.White
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Switch role") },
                                    leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                                    onClick = {
                                        showProfileMenu = false
                                        onSwitchRole()
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Column {
                            Text(
                                text = "${greetingForNow()},",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryGrey
                            )
                            Text(
                                text = MockPeople.helperName,
                                style = MaterialTheme.typography.titleMedium,
                                color = DarkNavy
                            )
                        }
                    }
                },
                actions = {
                    OnlineToggle(isOnline = isOnline, onToggle = { isOnline = !isOnline })
                    IconButton(onClick = {
                        scope.launch { snackbarHostState.showSnackbar("You're all caught up — no new notifications") }
                    }) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = DarkNavy)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MutedBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item { PerformanceOverviewCard() }

            item {
                SectionTitle("Today's summary")
                Spacer(modifier = Modifier.height(Spacing.sm))
                // 2 x 2 grid: every card fully visible, equal widths, no clipped carousel edge.
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        SummaryCard("New requests", allTasks.count { it.status == "submitted" }, Icons.Default.NewReleases, SuccessGreen, Modifier.weight(1f))
                        SummaryCard("Bids sent", allTasks.count { it.status == "pending" }, Icons.Default.LocalOffer, WarningAmber, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        SummaryCard("Active jobs", allTasks.count { it.status == "accepted" }, Icons.AutoMirrored.Filled.Assignment, BluePrimary, Modifier.weight(1f))
                        SummaryCard("Completed", allTasks.count { it.status == "completed" }, Icons.Default.CheckCircle, SecondaryGrey, Modifier.weight(1f))
                    }
                }
            }

            item {
                SectionTitle(
                    text = "Nearby jobs",
                    modifier = Modifier.padding(top = Spacing.xs),
                    trailing = {
                        TextButton(onClick = onSeeAllTasks, contentPadding = PaddingValues(horizontal = Spacing.xs)) {
                            Text("See all", style = MaterialTheme.typography.titleSmall, color = BluePrimary)
                        }
                    }
                )
            }

            when {
                !isOnline -> item {
                    EmptyState(
                        icon = Icons.Default.PowerSettingsNew,
                        title = "You're offline",
                        message = "Go online to start receiving job requests near you.",
                        actionLabel = "Go online",
                        onAction = { isOnline = true }
                    )
                }
                isLoading -> items(3) { JobCardSkeleton() }
                tasks.isEmpty() -> item {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "No jobs nearby right now",
                        message = "New requests will show up here as soon as customers post them."
                    )
                }
                else -> items(tasks, key = { it.id }) { task ->
                    WorkerJobCard(
                        task = task,
                        onClick = { sheet = JobSheet.Details(task) },
                        onPlaceBidClick = { sheet = JobSheet.Bid(task) },
                        onAccept = { viewModel.acceptTask(task) }
                    )
                }
            }
        }
    }

    JobSheetHost(sheet = sheet, onSheetChange = { sheet = it }, viewModel = viewModel, onOpenChat = onOpenChat)
}

@Composable
private fun rememberSaveableBoolean(initial: Boolean) =
    androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(initial) }

@Composable
private fun OnlineToggle(isOnline: Boolean, onToggle: () -> Unit) {
    val color = if (isOnline) SuccessGreen else SecondaryGrey
    Surface(
        onClick = onToggle,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isOnline) "Online" else "Offline",
                color = color,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SummaryCard(title: String, count: Int, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    FixhoraCard(modifier = modifier) {
        Row(modifier = Modifier.padding(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, tint = tint, size = 40.dp, iconSize = 22.dp)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Column {
                Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, color = DarkNavy)
                Text(text = title, style = MaterialTheme.typography.bodySmall, color = SecondaryGrey, maxLines = 1)
            }
        }
    }
}

@Composable
fun PerformanceOverviewCard() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(DarkNavy, Color(0xFF16326B))))
                .padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("This week", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.65f))
                    Text("Performance overview", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
                Surface(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = RatingGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("4.9", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            Row(modifier = Modifier.fillMaxWidth()) {
                PerformanceStat("Earnings", "₹8,450", Modifier.weight(1f))
                PerformanceStat("Jobs done", "142", Modifier.weight(1f))
                PerformanceStat("Avg. response", "< 5 min", Modifier.weight(1f))
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = Spacing.md))
            Row(modifier = Modifier.fillMaxWidth()) {
                ProgressStat("Acceptance", 0.95f, Modifier.weight(1f))
                ProgressStat("Completion", 0.98f, Modifier.weight(1f))
                ProgressStat("Satisfaction", 0.96f, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PerformanceStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 1)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.65f), maxLines = 1)
    }
}

@Composable
private fun ProgressStat(label: String, progress: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                color = Color(0xFF6EA0FF),
                trackColor = Color.White.copy(alpha = 0.12f),
                strokeWidth = 4.dp,
                modifier = Modifier.size(52.dp)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(text = label, color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun JobCardSkeleton() {
    val shimmer = BorderGrey.copy(alpha = 0.7f)
    FixhoraCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(shimmer))
                Spacer(modifier = Modifier.width(Spacing.sm))
                Column {
                    SkeletonLine(width = 120.dp, height = 14.dp, color = shimmer)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonLine(width = 80.dp, height = 10.dp, color = shimmer)
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            SkeletonLine(fraction = 0.75f, height = 16.dp, color = shimmer)
            Spacer(modifier = Modifier.height(Spacing.xs))
            SkeletonLine(fraction = 1f, height = 12.dp, color = shimmer)
            Spacer(modifier = Modifier.height(4.dp))
            SkeletonLine(fraction = 0.6f, height = 12.dp, color = shimmer)
            HorizontalDivider(color = DividerGrey, modifier = Modifier.padding(vertical = Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonLine(width = 96.dp, height = 18.dp, color = shimmer)
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.size(64.dp, 40.dp).clip(RoundedCornerShape(10.dp)).background(shimmer))
                Spacer(modifier = Modifier.width(Spacing.xs))
                Box(modifier = Modifier.size(80.dp, 40.dp).clip(RoundedCornerShape(10.dp)).background(shimmer))
            }
        }
    }
}

@Composable
private fun SkeletonLine(color: Color, height: androidx.compose.ui.unit.Dp, width: androidx.compose.ui.unit.Dp? = null, fraction: Float = 1f) {
    Box(
        modifier = (if (width != null) Modifier.width(width) else Modifier.fillMaxWidth(fraction))
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}
