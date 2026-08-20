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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.repository.ChatRepository
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.ui.components.EmptyState
import com.example.ui.screens.taskflow.dummyCategories

@Composable
fun WorkerChatScreen(viewModel: HelperViewModel) {
    val allTasks by viewModel.allTasks.collectAsState()
    val activeTasks =
        allTasks.filter { it.status.isActiveEngagement || it.status == TaskStatus.COMPLETED }
    var selectedTask by remember { mutableStateOf<TaskEntity?>(null) }

    if (selectedTask == null) {
        WorkerChatListScreen(
            tasks = activeTasks,
            onChatClick = { selectedTask = it }
        )
    } else {
        WorkerChatConversationScreen(
            task = selectedTask!!,
            viewModel = viewModel,
            onBack = { selectedTask = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerChatListScreen(tasks: List<TaskEntity>, onChatClick: (TaskEntity) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Unread", "Active Jobs", "Pending Bids", "Accepted", "Completed")
    var selectedCategory by remember { mutableStateOf("All") }

    Column(modifier = Modifier.fillMaxSize().background(FixTheme.colors.surface)) {
        TopAppBar(
            title = { Text("Chats", fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary) },
            actions = {
                IconButton(onClick = { /* Filter */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = FixTheme.colors.textPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.surface)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by customer, job...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = FixTheme.colors.border,
                focusedBorderColor = FixTheme.colors.primary,
                unfocusedContainerColor = FixTheme.colors.surfaceAlt,
                focusedContainerColor = FixTheme.colors.surface
            ),
            singleLine = true
        )

        // Categories
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FixTheme.colors.primary,
                        selectedLabelColor = FixTheme.colors.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategory == category,
                        borderColor = if (selectedCategory == category) FixTheme.colors.primary else FixTheme.colors.border
                    )
                )
            }
        }

        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.ChatBubbleOutline,
                    title = "No conversations yet",
                    description = "Once you accept a job, your chat with that customer appears here."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks) { task ->
                    ChatListItem(task = task, onClick = { onChatClick(task) })
                }
            }
        }
    }
}

@Composable
fun ChatListItem(task: TaskEntity, onClick: () -> Unit) {
    val category = dummyCategories.find { it.id == task.categoryId }?.title ?: "General Service"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(FixTheme.colors.border),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = FixTheme.colors.textSecondary, modifier = Modifier.size(32.dp))
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(FixTheme.colors.success)
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Customer Request", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FixTheme.colors.textPrimary)
                }
                Text("Just now", fontSize = 12.sp, color = FixTheme.colors.textSecondary, fontWeight = FontWeight.Normal)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = FixTheme.colors.infoSurface,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(category, color = FixTheme.colors.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tap to view conversation",
                    fontSize = 14.sp,
                    color = FixTheme.colors.textSecondary,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerChatConversationScreen(task: TaskEntity, viewModel: HelperViewModel, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val category = dummyCategories.find { it.id == task.categoryId }?.title ?: "General Service"
    val quickReplies = listOf("I can do this today.", "My estimated cost is...", "I'll arrive in 20 minutes.", "Work has been completed.")
    val quickActions = listOf("Send Quote", "Accept Job", "Share Location", "Send Invoice")

    val messages by viewModel.getChatMessages(task.id).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().background(FixTheme.colors.surfaceAlt)) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(FixTheme.colors.border),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = FixTheme.colors.textSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Customer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FixTheme.colors.textPrimary)
                        }
                        Text("Online", fontSize = 12.sp, color = FixTheme.colors.success)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Call */ }) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = FixTheme.colors.primary)
                }
                IconButton(onClick = { /* More */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = FixTheme.colors.textPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.surface)
        )

        // Pinned Job Info Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = FixTheme.colors.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Job: ${task.descriptionTitle.ifEmpty { category }}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FixTheme.colors.textPrimary)
                    Text("Budget: ₹${task.minBudget} - ₹${task.maxBudget}", fontSize = 12.sp, color = FixTheme.colors.textSecondary)
                }
                TextButton(onClick = { /* View Details */ }) {
                    Text("View Details", fontSize = 12.sp)
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(messages) { message ->
                MessageBubble(
                    text = message.text,
                    isSender = message.senderId == ChatRepository.SENDER_WORKER,
                    time = "Just now"
                )
            }
            item {
                MessageBubble(text = "Hello! I saw your job request for ${task.descriptionTitle.ifEmpty { category }}. I'm available today.", isSender = false, time = "10:28 AM")
            }
        }

        // Quick Actions & Replies
        Column(modifier = Modifier.fillMaxWidth().background(FixTheme.colors.surface)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickActions) { action ->
                    AssistChip(
                        onClick = { /* Action */ },
                        label = { Text(action, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                        colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = FixTheme.colors.primary),
                        leadingIcon = {
                            Icon(
                                imageVector = if (action.contains("Quote") || action.contains("Invoice")) Icons.Default.Receipt 
                                else if (action.contains("Accept")) Icons.Default.CheckCircle
                                else Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickReplies) { reply ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FixTheme.colors.surfaceAlt,
                        modifier = Modifier.clickable { messageText = reply }
                    ) {
                        Text(reply, fontSize = 13.sp, color = FixTheme.colors.textPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
            
            HorizontalDivider(color = FixTheme.colors.border)

            // Input Area
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Attach */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = FixTheme.colors.textSecondary)
                }
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = FixTheme.colors.surfaceAlt,
                        unfocusedContainerColor = FixTheme.colors.surfaceAlt
                    ),
                    maxLines = 4
                )

                if (messageText.isBlank()) {
                    IconButton(onClick = { /* Voice */ }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Message", tint = FixTheme.colors.primary)
                    }
                } else {
                    IconButton(onClick = {
                        viewModel.sendMessage(task.id, messageText)
                        messageText = ""
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = FixTheme.colors.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, isSender: Boolean, time: String, status: String? = null) {
    val alignment = if (isSender) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isSender) FixTheme.colors.primary else FixTheme.colors.surface
    val textColor = if (isSender) FixTheme.colors.onPrimary else FixTheme.colors.textPrimary
    val shape = if (isSender) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = alignment) {
        Column(horizontalAlignment = if (isSender) Alignment.End else Alignment.Start) {
            Surface(
                shape = shape,
                color = bgColor,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(time, fontSize = 11.sp, color = FixTheme.colors.textSecondary)
                if (isSender && status != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = status,
                        tint = if (status == "Read") FixTheme.colors.primary else FixTheme.colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
