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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import com.example.data.room.TaskEntity
import com.example.ui.screens.taskflow.dummyCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHomeScreen(viewModel: HelperViewModel) {
    val tasks by viewModel.availableTasks.collectAsState()
    var isOnline by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BorderGrey),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = SecondaryGrey)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Good Morning,", fontSize = 12.sp, color = SecondaryGrey)
                            Text(text = "Alex Worker", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                        }
                    }
                },
                actions = {
                    Surface(
                        color = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { isOnline = !isOnline }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isOnline) Color(0xFF2E7D32) else Color(0xFFD32F2F)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isOnline) "Online" else "Offline", color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = { /* Notifications */ }) {
                        BadgedBox(badge = { Badge { Text("2") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = DarkNavy)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                PerformanceOverviewCard()
            }

            item {
                Text(
                    text = "Today's Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { SummaryCard("Today's Tasks", "3", Icons.Default.Assignment, Color(0xFFE3F2FD), BluePrimary) }
                    item { SummaryCard("Completed", "1", Icons.Default.CheckCircle, Color(0xFFE8F5E9), Color(0xFF2E7D32)) }
                    item { SummaryCard("Pending", "2", Icons.Default.PendingActions, Color(0xFFFFF3E0), OrangeSecondary) }
                    item { SummaryCard("Proposed", "5", Icons.Default.LocalOffer, Color(0xFFF3E5F5), Color(0xFF8E24AA)) }
                    item { SummaryCard("Missed", "0", Icons.Default.Cancel, Color(0xFFFFEBEE), Color(0xFFD32F2F)) }
                }
            }

            item {
                Text(
                    text = "Available Nearby Jobs",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
            }

            if (isLoading) {
                items(3) {
                    JobCardSkeleton()
                }
            } else if (tasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = SecondaryGrey.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No nearby jobs available at the moment.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = SecondaryGrey
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { /* Refresh */ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(tasks) { task ->
                    WorkerJobCard(
                        task = task, 
                        onSubmitProposalClick = { /* Handle Proposal */ },
                        onAccept = { viewModel.acceptTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, count: String, icon: ImageVector, bgColor: Color, iconColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(120.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = count, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 12.sp, color = DarkNavy.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun PerformanceOverviewCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Performance Overview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("4.9", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PerformanceStat("Today's Earnings", "₹1,250")
                PerformanceStat("Jobs Completed", "142")
                PerformanceStat("Response Time", "< 5m")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ProgressStat("Acceptance", 0.95f, "95%")
                ProgressStat("Completion", 0.98f, "98%")
                ProgressStat("Satisfaction", 0.96f, "96%")
            }
        }
    }
}

@Composable
fun PerformanceStat(label: String, value: String) {
    Column {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun ProgressStat(label: String, progress: Float, percentage: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            CircularProgressIndicator(
                progress = { progress },
                color = BluePrimary,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(text = percentage, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

@Composable
fun JobCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Box(modifier = Modifier.height(16.dp).width(120.dp).background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.height(12.dp).width(80.dp).background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.height(20.dp).fillMaxWidth(0.8f).background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.height(14.dp).fillMaxWidth().background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.height(14.dp).fillMaxWidth(0.6f).background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.height(14.dp).width(100.dp).background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.height(14.dp).width(60.dp).background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderGrey)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f).height(48.dp).background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)))
                Box(modifier = Modifier.weight(1f).height(48.dp).background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)))
            }
        }
    }
}

@Composable
fun WorkerJobCard(task: TaskEntity, onSubmitProposalClick: () -> Unit, onAccept: () -> Unit) {
    val category = dummyCategories.find { it.id == task.categoryId } ?: dummyCategories.first()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(BorderGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = SecondaryGrey, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Customer Name", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = BluePrimary, modifier = Modifier.size(14.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("4.8", fontSize = 12.sp, color = SecondaryGrey)
                            Text(" • 10 mins ago", fontSize = 12.sp, color = SecondaryGrey)
                        }
                    }
                }
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Available", color = BluePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = task.descriptionTitle.ifEmpty { "Need Help with ${category.title}" },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.descriptionDetails.ifEmpty { "Looking for someone to help me out with this task." },
                color = SecondaryGrey,
                fontSize = 14.sp,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryGrey, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (task.useCurrentLocation) "2.5 km away" else task.locationQuery.ifEmpty { "Not specified" }, color = SecondaryGrey, fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = SecondaryGrey, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Urgent", color = OrangeSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val budgetText = if (task.minBudget.isNotBlank() && task.maxBudget.isNotBlank()) "₹${task.minBudget} - ₹${task.maxBudget}" else if (task.minBudget.isNotBlank()) "Min ₹${task.minBudget}" else if (task.maxBudget.isNotBlank()) "Max ₹${task.maxBudget}" else "Budget Negotiable"
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(budgetText, color = DarkNavy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderGrey)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onSubmitProposalClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary)
                ) {
                    Text("Place Bid", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Accept Job", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
