package com.example.ui.screens.helper

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.room.ChatMessageEntity
import com.example.data.room.TaskEntity
import com.example.ui.components.BackButton
import com.example.ui.components.EmptyState
import com.example.ui.components.FixhoraCard
import com.example.ui.components.FixhoraTextField
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.MockPeople
import com.example.ui.components.Spacing
import com.example.ui.components.StatusChip
import com.example.ui.components.budgetLabel
import com.example.ui.components.clockTime
import com.example.ui.components.dayLabel
import com.example.ui.components.relativeTime
import com.example.ui.screens.taskflow.categoryFor
import com.example.ui.theme.*

private val chatStatuses = setOf("pending", "accepted", "completed")

private enum class ChatFilter(val label: String, val statuses: Set<String>) {
    All("All", chatStatuses),
    Active("Active", setOf("accepted")),
    Bids("Bids", setOf("pending")),
    Completed("Completed", setOf("completed")),
}

@Composable
fun WorkerChatScreen(viewModel: HelperViewModel, onExploreJobs: () -> Unit) {
    val allTasks by viewModel.allTasks.collectAsState()
    val openChatTaskId by viewModel.openChatTaskId.collectAsState()
    val openTask = allTasks.find { it.id == openChatTaskId }

    if (openTask == null) {
        WorkerChatListScreen(
            tasks = allTasks.filter { it.status in chatStatuses },
            viewModel = viewModel,
            onChatClick = { viewModel.openChat(it.id) },
            onExploreJobs = onExploreJobs
        )
    } else {
        BackHandler { viewModel.closeChat() }
        WorkerChatConversationScreen(
            task = openTask,
            viewModel = viewModel,
            onBack = { viewModel.closeChat() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerChatListScreen(
    tasks: List<TaskEntity>,
    viewModel: HelperViewModel,
    onChatClick: (TaskEntity) -> Unit,
    onExploreJobs: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChatFilter.All) }

    val visible = tasks.filter { task ->
        task.status in filter.statuses && (searchQuery.isBlank() ||
            MockPeople.customerName(task.id).contains(searchQuery, ignoreCase = true) ||
            task.descriptionTitle.contains(searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        HelperTopBar(title = { Text("Chats", style = MaterialTheme.typography.titleLarge, color = DarkNavy) })

        FixhoraTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search by customer or job",
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
            items(ChatFilter.entries.toList()) { option ->
                val count = tasks.count { it.status in option.statuses }
                FilterChip(
                    selected = filter == option,
                    onClick = { filter = option },
                    label = { Text("${option.label} ($count)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        labelColor = DarkNavy,
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filter == option,
                        borderColor = BorderGrey,
                        selectedBorderColor = BluePrimary
                    )
                )
            }
        }
        HorizontalDivider(color = DividerGrey)

        if (visible.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (tasks.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.ChatBubbleOutline,
                        title = "No conversations yet",
                        message = "Bid on or accept a job and your chats with customers will appear here.",
                        actionLabel = "Explore nearby jobs",
                        onAction = onExploreJobs
                    )
                } else {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "No chats found",
                        message = "Try a different name or filter."
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(visible, key = { it.id }) { task ->
                    ChatListItem(task = task, viewModel = viewModel, onClick = { onChatClick(task) })
                    HorizontalDivider(color = DividerGrey, modifier = Modifier.padding(start = 84.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(task: TaskEntity, viewModel: HelperViewModel, onClick: () -> Unit) {
    val customer = MockPeople.customerName(task.id)
    val messages by remember(task.id) { viewModel.getChatMessages(task.id) }.collectAsState(initial = emptyList())
    val last = messages.firstOrNull()
    val status = statusStyle(task.status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            InitialsAvatar(name = customer, size = 52.dp)
            if (task.status == "accepted") {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                )
            }
        }
        Spacer(modifier = Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    customer,
                    style = MaterialTheme.typography.titleSmall,
                    color = DarkNavy,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = last?.let { relativeTime(it.timestamp) } ?: relativeTime(task.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = HintGrey
                )
            }
            Text(
                text = task.descriptionTitle,
                style = MaterialTheme.typography.bodySmall,
                color = BluePrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = last?.let { (if (it.senderId == "worker") "You: " else "") + it.text } ?: MockPeople.openingMessage(task),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                StatusChip(text = status.label, color = status.color)
            }
        }
    }
}

@Composable
private fun WorkerChatConversationScreen(task: TaskEntity, viewModel: HelperViewModel, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val customer = MockPeople.customerName(task.id)
    val category = categoryFor(task.categoryId)
    val status = statusStyle(task.status)
    val quickReplies = listOf(
        "I can do this today.",
        "Could you share a photo?",
        "I'll arrive in 20 minutes.",
        "The work is completed."
    )
    val messages by remember(task.id) { viewModel.getChatMessages(task.id) }.collectAsState(initial = emptyList())

    fun send() {
        val text = messageText.trim()
        if (text.isNotEmpty()) {
            viewModel.sendMessage(task.id, text)
            messageText = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MutedBackground)
    ) {
        HelperTopBar(
            navigationIcon = { BackButton(onClick = onBack, modifier = Modifier.padding(start = Spacing.xxs)) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name = customer, size = 38.dp)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Column {
                        Text(customer, style = MaterialTheme.typography.titleSmall, color = DarkNavy)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Online", style = MaterialTheme.typography.bodySmall, color = SecondaryGrey)
                        }
                    }
                }
            }
        )
        HorizontalDivider(color = DividerGrey)

        // Pinned job summary
        FixhoraCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            Row(
                modifier = Modifier.padding(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.descriptionTitle.ifBlank { category?.title ?: "Task" },
                        style = MaterialTheme.typography.titleSmall,
                        color = DarkNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${category?.title ?: "General"} · ${task.budgetLabel()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.xs))
                StatusChip(text = status.label, color = status.color)
            }
        }

        // Messages (newest at the bottom)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
            reverseLayout = true
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }
            item {
                MessageBubble(
                    text = MockPeople.openingMessage(task),
                    isSender = false,
                    time = clockTime(task.createdAt)
                )
            }
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm), contentAlignment = Alignment.Center) {
                    Surface(color = Color.White, shape = RoundedCornerShape(50)) {
                        Text(
                            dayLabel(task.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryGrey,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Quick replies + composer
        Surface(color = Color.White) {
            Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                HorizontalDivider(color = DividerGrey)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    items(quickReplies) { reply ->
                        Surface(
                            onClick = { messageText = reply },
                            shape = RoundedCornerShape(50),
                            color = MutedBackground
                        ) {
                            Text(
                                reply,
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkNavy,
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Spacing.md, end = Spacing.xs, bottom = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type a message…", color = HintGrey) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MutedSurface,
                            unfocusedContainerColor = MutedSurface,
                            cursorColor = BluePrimary,
                            focusedTextColor = DarkNavy,
                            unfocusedTextColor = DarkNavy
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(Spacing.xxs))
                    FilledIconButton(
                        onClick = { send() },
                        enabled = messageText.isNotBlank(),
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = BluePrimary,
                            contentColor = Color.White,
                            disabledContainerColor = BluePrimary.copy(alpha = 0.3f),
                            disabledContentColor = Color.White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessageEntity) {
    MessageBubble(
        text = message.text,
        isSender = message.senderId == "worker",
        time = clockTime(message.timestamp)
    )
}

@Composable
fun MessageBubble(text: String, isSender: Boolean, time: String) {
    val shape = if (isSender) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxs),
        contentAlignment = if (isSender) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (isSender) Alignment.End else Alignment.Start) {
            Surface(
                shape = shape,
                color = if (isSender) BluePrimary else Color.White,
                shadowElevation = if (isSender) 0.dp else 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = text,
                    color = if (isSender) Color.White else DarkNavy,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(time, style = MaterialTheme.typography.labelSmall, color = HintGrey)
        }
    }
}
