package com.example.ui.screens.helper

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.ChatRepository
import com.example.data.room.TaskEntity
import com.example.ui.components.ChatInputBar
import com.example.ui.components.EmptyState
import com.example.ui.components.FixTopBar
import com.example.ui.components.MessageBubble
import com.example.ui.components.SearchField
import com.example.ui.components.StatusBadge
import com.example.ui.format.budgetText
import com.example.ui.format.relativeTimeText
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*

@Composable
fun WorkerChatScreen(viewModel: HelperViewModel, onBack: () -> Unit = {}) {
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
            onChatClick = { selectedTaskId = it.id },
            onBack = onBack
        )
    } else {
        // Back from a conversation returns to the list. Without this it left the helper side
        // altogether, dropping the user on the role choice mid-conversation.
        BackHandler { selectedTaskId = null }
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
    onChatClick: (TaskEntity) -> Unit,
    onBack: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().background(FixTheme.colors.background)) {
        // The filter icon and the "All / Unread / Pending Bids" chips are gone: read state and
        // bids do not exist in this app, so those filters could never have done anything.
        FixTopBar(title = stringResource(R.string.chat_title), onBack = onBack)

        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(R.string.chat_search_hint),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                fontSize = 13.sp,
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

    Column(modifier = Modifier.fillMaxSize().background(FixTheme.colors.background)) {
        // Was a hardcoded green "Online" under the title. Presence is not tracked; the budget is
        // what the helper actually needs while talking. Call and "More" are gone too: there is no
        // phone number stored and no menu to show.
        FixTopBar(
            title = task.descriptionTitle.ifBlank { category?.let { stringResource(it.titleRes) } ?: stringResource(R.string.task_conversation) },
            subtitle = budgetText(task.minBudget, task.maxBudget),
            onBack = onBack,
            backDescription = stringResource(R.string.cd_back_to_chats)
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

        ChatInputBar(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = stringResource(R.string.chat_input_hint),
            onSend = {
                viewModel.sendMessage(task.id, it)
                messageText = ""
            }
        )
    }
}
