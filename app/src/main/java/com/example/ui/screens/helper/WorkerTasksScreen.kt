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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.automirrored.filled.Sort
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
import com.example.util.IndiaFormat
import com.example.data.room.TaskEntity
import com.example.ui.screens.taskflow.dummyCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerTasksScreen(viewModel: HelperViewModel, onExploreJobs: () -> Unit = {}) {
    var sortByBudget by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val allTasks by viewModel.allTasks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    // Only statuses that really exist are offered as filters.
    val tabs = listOf("All Tasks", "New Requests", "Accepted", "Completed", "Rejected")
    var selectedTab by remember { mutableStateOf("All Tasks") }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
        topBar = {
            TopAppBar(
                title = { Text("My Tasks", fontWeight = FontWeight.Bold, color = DarkNavy) },
                actions = {
                    // Search box and status chips are below; only Sort needs a menu.
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort tasks", tint = DarkNavy)
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Newest first") },
                                leadingIcon = { if (!sortByBudget) Icon(Icons.Default.Check, contentDescription = null) },
                                onClick = { sortByBudget = false; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Highest budget first") },
                                leadingIcon = { if (sortByBudget) Icon(Icons.Default.Check, contentDescription = null) },
                                onClick = { sortByBudget = true; showSortMenu = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFF8F9FA))) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tasks or locations", color = SecondaryGrey, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SecondaryGrey) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderGrey,
                    focusedBorderColor = BluePrimary,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
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
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = DarkNavy
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) BluePrimary else BorderGrey
                        )
                    )
                }
            }

            // Task List
            val query = searchQuery.trim()
            val filteredTasks = allTasks.filter { task ->
                val statusMatch = when (selectedTab) {
                    "New Requests" -> task.status == "submitted"
                    "Accepted" -> task.status == "accepted"
                    "Completed" -> task.status == "completed"
                    "Rejected" -> task.status == "rejected"
                    else -> true
                }
                val searchMatch = query.isEmpty() ||
                    task.descriptionTitle.contains(query, ignoreCase = true) ||
                    task.descriptionDetails.contains(query, ignoreCase = true) ||
                    task.locationQuery.contains(query, ignoreCase = true)
                statusMatch && searchMatch
            }.let { list ->
                // Database already returns newest first.
                if (sortByBudget) list.sortedByDescending { (it.maxBudget.toLongOrNull() ?: it.minBudget.toLongOrNull()) ?: -1L } else list
            }

            if (filteredTasks.isEmpty()) {
                // Empty State
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = BorderGrey
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No tasks found. New customer requests will appear here as soon as they are available.",
                            color = SecondaryGrey,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onExploreJobs,
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Explore Nearby Jobs")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
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
    val budgetText = IndiaFormat.budgetLabel(task.minBudget, task.maxBudget)
    val displayStatus = when (task.status) {
        "submitted" -> "New"
        "pending" -> "Pending"
        "accepted" -> "Accepted"
        "completed" -> "Completed"
        "rejected" -> "Rejected"
        else -> "Draft"
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* View Details */ },
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(BorderGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = SecondaryGrey)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Customer Request", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = OrangeSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New User", fontSize = 12.sp, color = SecondaryGrey, fontWeight = FontWeight.Medium)
                            Text(" • Just now", fontSize = 12.sp, color = SecondaryGrey)
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    // Status Badge
                    Surface(
                        color = when(displayStatus) {
                            "New" -> Color(0xFFE8F5E9)
                            "Pending" -> Color(0xFFFFF3E0)
                            "Accepted" -> Color(0xFFE3F2FD)
                            else -> BorderGrey
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = displayStatus,
                            color = when(displayStatus) {
                                "New" -> Color(0xFF2E7D32)
                                "Pending" -> Color(0xFFEF6C00)
                                "Accepted" -> BluePrimary
                                else -> DarkNavy
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Job Details
            Text(task.descriptionTitle.ifEmpty { "Need Help with $category" }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkNavy)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                task.descriptionDetails.ifEmpty { "Looking for someone to help me out with this task." },
                fontSize = 14.sp,
                color = SecondaryGrey,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Job Metadata Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    TaskMetaRow(Icons.Default.Category, category)
                    Spacer(modifier = Modifier.height(4.dp))
                    TaskMetaRow(Icons.Default.LocationOn, task.locationQuery.ifBlank { "Location not shared" })
                }
                Column(modifier = Modifier.weight(1f)) {
                    TaskMetaRow(Icons.Default.AccountBalanceWallet, budgetText)
                    Spacer(modifier = Modifier.height(4.dp))
                    TaskMetaRow(Icons.Default.Schedule, "ASAP")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderGrey)
            Spacer(modifier = Modifier.height(12.dp))

            // Actions based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (displayStatus == "New") {
                    Text("0 bids", fontSize = 12.sp, color = SecondaryGrey, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Text("Reject", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onAccept,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
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
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
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
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
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
        Icon(icon, contentDescription = null, tint = SecondaryGrey, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = DarkNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
