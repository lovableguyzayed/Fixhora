package com.example.ui.screens.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.MessageBubble
import com.example.ui.format.budgetText
import com.example.ui.format.relativeTimeText
import com.example.data.repository.ChatRepository
import com.example.data.room.TaskEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*

@Composable
fun WorkerChatScreen(viewModel: HelperViewModel) {
    val conversations by viewModel.conversations.collectAsState()
    val query by viewModel.chatQuery.collectAsState()
    val pendingChatTaskId by viewModel.pendingChatTaskId.collectAsState()
    var selectedTaskId by remember { mutableStateOf<Int?>(null) }

    // A "Chat" tap on a job card lands here; consuming it stops the screen reopening that thread
    // every time the tab is revisited.
    LaunchedEffect(pendingChatTaskId) {
        pendingChatTaskId?.let {
            selectedTaskId = it
            viewModel.consumePendingChat()
        }
    }

    val allTasks by viewModel.allTasks.collectAsState()
    val selectedTask = selectedTaskId?.let { id -> allTasks.find { it.id == id } }

    if (selectedTask == null) {
        WorkerChatListScreen(
            tasks = conversations,
            query = query,
            onQueryChange = viewModel::onChatQueryChange,
            onChatClick = { selectedTaskId = it.id }
        )
    } else {
        WorkerChatConversationScreen(
            task = selectedTask,
            viewModel = viewModel,
            onBack = { selectedTaskId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerChatListScreen(
    tasks: List<TaskEntity>,
    query: String,
    onQueryChange: (String) -> Unit,
    onChatClick: (TaskEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(FixTheme.colors.surfaceAlt)) {
        TopAppBar(
            title = { Text(stringResource(R.string.chat_title), fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary) },
            // The filter icon and the "All / Unread / Pending Bids" chips are gone: read state and
            // bids do not exist in this app, so those filters could never have done anything.
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.surface)
        )

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.chat_search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FixTheme.colors.textSecondary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_clear_search))
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

        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.ChatBubbleOutline,
                    title =
                        if (query.isNotBlank()) {
                            stringResource(R.string.chat_empty_query_title, query)
                        } else {
                            stringResource(R.string.chat_empty_title)
                        },
                    description = if (query.isNotBlank()) {
                        stringResource(R.string.chat_empty_query_body)
                    } else {
                        stringResource(R.string.chat_empty_body)
                    },
                    actionText = if (query.isNotBlank()) stringResource(R.string.action_clear_search) else null,
                    onAction = if (query.isNotBlank()) ({ onQueryChange("") }) else null
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks, key = { it.id }) { task ->
                    ChatListItem(task = task, onClick = { onChatClick(task) })
                    HorizontalDivider(color = FixTheme.colors.border)
                }
            }
        }
    }
}

@Composable
fun ChatListItem(task: TaskEntity, onClick: () -> Unit) {
    val category = dummyCategories.find { it.id == task.categoryId }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FixTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(FixTheme.colors.surfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = FixTheme.colors.textSecondary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.descriptionTitle.ifBlank { category?.let { stringResource(it.titleRes) } ?: stringResource(R.string.task_untitled) },
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FixTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                relativeTimeText(task.createdAt),
                fontSize = 12.sp,
                color = FixTheme.colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        StatusBadge(text = task.status.displayLabel(), tone = task.status.tone())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerChatConversationScreen(task: TaskEntity, viewModel: HelperViewModel, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val category = dummyCategories.find { it.id == task.categoryId }
    val messages by viewModel.getChatMessages(task.id).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().background(FixTheme.colors.surfaceAlt)) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        task.descriptionTitle.ifBlank { category?.let { stringResource(it.titleRes) } ?: stringResource(R.string.task_conversation) },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = FixTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Was a hardcoded green "Online". Presence is not tracked.
                    Text(
                        budgetText(task.minBudget, task.maxBudget),
                        fontSize = 12.sp,
                        color = FixTheme.colors.textSecondary
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back_to_chats))
                }
            },
            // Call and "More" removed: there is no phone number stored and no menu to show.
            colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.surface)
        )

        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.ChatBubbleOutline,
                    title = stringResource(R.string.chat_thread_empty_title),
                    description = stringResource(R.string.chat_thread_empty_body)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                reverseLayout = true
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                // The list used to end with a fabricated "Hello! I saw your job request…" bubble
                // injected into every conversation, attributed to the customer.
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        text = message.text,
                        isSender = message.senderId == ChatRepository.SENDER_WORKER,
                        time = relativeTimeText(message.timestamp)
                    )
                }
            }
        }

        Surface(color = FixTheme.colors.surface) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FixTheme.colors.primary,
                        unfocusedBorderColor = FixTheme.colors.border,
                        focusedContainerColor = FixTheme.colors.surfaceAlt,
                        unfocusedContainerColor = FixTheme.colors.surfaceAlt
                    ),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Attach and voice-note buttons removed: neither was implemented.
                IconButton(
                    onClick = {
                        viewModel.sendMessage(task.id, messageText)
                        messageText = ""
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.cd_send_message),
                        tint = if (messageText.isNotBlank()) FixTheme.colors.primary else FixTheme.colors.onDisabled
                    )
                }
            }
        }
    }
}
