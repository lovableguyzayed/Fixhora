package com.example.ui.screens.shell

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.room.ChatMessageEntity
import com.example.ui.theme.*
import com.example.util.IndiaFormat
import kotlinx.coroutines.flow.Flow

/** One conversation per task, between the customer and the chosen helper. */
data class ChatThread(val taskId: Int, val taskTitle: String, val otherName: String, val budget: String)

/**
 * Chat list + conversation. [me] is "customer" or "worker" (the sender id stored with messages).
 * Until a server exists, messages only live on this phone.
 */
@Composable
fun ChatsScreen(
    threads: List<ChatThread>?,
    me: String,
    emptyText: String,
    quickReplies: List<String>,
    messagesFor: (Int) -> Flow<List<ChatMessageEntity>>,
    onSend: (taskId: Int, text: String) -> Unit,
    openTaskId: Int? = null,
    onOpenConsumed: () -> Unit = {},
) {
    var openId by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(openTaskId) { if (openTaskId != null) { openId = openTaskId; onOpenConsumed() } }
    val open = threads?.firstOrNull { it.taskId == openId }
    // System Back closes the open conversation first.
    BackHandler(enabled = open != null) { openId = null }

    if (open == null) {
        Column(Modifier.fillMaxSize().background(Color.White)) {
            Text(stringResource(R.string.nav_chats), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkNavy,
                modifier = Modifier.padding(16.dp))
            if (threads != null && threads.isEmpty()) {
                Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = BorderGrey, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(emptyText, color = SecondaryGrey, textAlign = TextAlign.Center, fontSize = 15.sp)
                }
            } else {
                LazyColumn {
                    items(threads.orEmpty(), key = { it.taskId }) { t ->
                        Row(Modifier.fillMaxWidth().clickable { openId = t.taskId }.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            InitialAvatar(t.otherName, size = 48.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t.otherName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                                Text(t.taskTitle, color = SecondaryGrey, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        HorizontalDivider(color = BorderGrey)
                    }
                }
            }
        }
    } else {
        Conversation(open, me, quickReplies, messagesFor(open.taskId), { onSend(open.taskId, it) }, onBack = { openId = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Conversation(
    thread: ChatThread,
    me: String,
    quickReplies: List<String>,
    messagesFlow: Flow<List<ChatMessageEntity>>,
    onSend: (String) -> Unit,
    onBack: () -> Unit,
) {
    val messages by messagesFlow.collectAsState(initial = emptyList())
    var text by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(PageBg).imePadding()) {
        TopAppBar(
            title = {
                Column {
                    Text(thread.otherName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DarkNavy)
                    Text("${thread.taskTitle} · ${thread.budget}", fontSize = 12.sp, color = SecondaryGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.mobilelogin_back)) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), reverseLayout = true) {
            item { Spacer(Modifier.height(12.dp)) }
            items(messages, key = { it.id }) { m -> Bubble(m.text, m.senderId == me, IndiaFormat.chatTime(m.timestamp)) }
        }
        Column(Modifier.fillMaxWidth().background(Color.White)) {
            if (quickReplies.isNotEmpty()) {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickReplies) { r -> SuggestionChip(onClick = { text = r }, label = { Text(r) }) }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text, onValueChange = { text = it.take(1000) },
                    placeholder = { Text(stringResource(R.string.chat_type_message)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary, unfocusedBorderColor = BorderGrey)
                )
                IconButton(onClick = { if (text.isNotBlank()) { onSend(text.trim()); text = "" } }, enabled = text.isNotBlank(),
                    modifier = Modifier.size(52.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.chat_send), tint = if (text.isNotBlank()) BluePrimary else SecondaryGrey)
                }
            }
        }
    }
}

@Composable
private fun Bubble(text: String, mine: Boolean, time: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
            Surface(
                color = if (mine) BluePrimary else Color.White,
                shape = if (mine) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                shadowElevation = 1.dp, modifier = Modifier.widthIn(max = 300.dp)
            ) { Text(text, color = if (mine) Color.White else DarkNavy, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) }
            Text(time, fontSize = 12.sp, color = SecondaryGrey, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
