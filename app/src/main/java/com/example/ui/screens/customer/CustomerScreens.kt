package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.market.Offer
import com.example.ui.screens.shell.*
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.screens.taskflow.title
import com.example.ui.theme.*
import com.example.util.IndiaFormat

/** Customer Home: greeting and the categories. Tapping one starts "Post a task" with it chosen. */
@Composable
fun CustomerHomeScreen(name: String?, onPostTask: (String?) -> Unit) {
    val scale = LocalConfiguration.current.fontScale.coerceIn(1f, 1.6f)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp * scale),
        modifier = Modifier.fillMaxSize().background(PageBg),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(
                    if (name != null) stringResource(R.string.customer_greeting, name) else stringResource(R.string.customer_greeting_guest),
                    color = SecondaryGrey, fontSize = 15.sp
                )
                Text(stringResource(R.string.customer_home_question), fontSize = 22.sp, lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold, color = DarkNavy)
                Text(stringResource(R.string.customer_home_sub), color = SecondaryGrey, fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
            }
        }
        items(dummyCategories, key = { it.id }) { c ->
            Surface(
                shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, BorderGrey),
                modifier = Modifier.heightIn(min = 112.dp).clickable { onPostTask(c.id) }
            ) {
                Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(Modifier.size(48.dp).background(c.iconTint.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(c.icon, contentDescription = null, tint = c.iconTint)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(c.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = DarkNavy, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private enum class TaskFilter { Active, Done, Cancelled }

@Composable
fun MyTasksScreen(
    tasks: List<TaskWithOffers>?,
    onPostTask: () -> Unit,
    onChoose: (TaskWithOffers, Offer) -> Unit,
    onMarkDone: (TaskWithOffers) -> Unit,
    onCancel: (TaskWithOffers) -> Unit,
    onChat: (TaskWithOffers) -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf(TaskFilter.Active) }
    var choosing by remember { mutableStateOf<Pair<TaskWithOffers, Offer>?>(null) }
    var cancelling by remember { mutableStateOf<TaskWithOffers?>(null) }
    val all = tasks.orEmpty()
    val active = all.filter { it.task.status == "submitted" || it.task.status == "accepted" }
    val done = all.filter { it.task.status == "completed" }
    val cancelled = all.filter { it.task.status == "cancelled" }

    Box(Modifier.fillMaxSize().background(PageBg)) {
        Column(Modifier.fillMaxSize()) {
            Text(stringResource(R.string.nav_my_tasks), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkNavy,
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp))
            if (tasks != null && all.isEmpty()) {
                EmptyTasks(onPostTask)
                return@Column
            }
            // Scrolls sideways when the font is large, instead of squeezing the chips.
            Row(Modifier.fillMaxWidth().background(Color.White).horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(filter == TaskFilter.Active, { filter = TaskFilter.Active }, { Text(stringResource(R.string.mytasks_active, active.size)) })
                FilterChip(filter == TaskFilter.Done, { filter = TaskFilter.Done }, { Text(stringResource(R.string.mytasks_done, done.size)) })
                FilterChip(filter == TaskFilter.Cancelled, { filter = TaskFilter.Cancelled }, { Text(stringResource(R.string.mytasks_cancelled)) })
            }
            val shown = when (filter) { TaskFilter.Active -> active; TaskFilter.Done -> done; TaskFilter.Cancelled -> cancelled }
            if (tasks == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (shown.isEmpty()) {
                Text(stringResource(R.string.mytasks_empty_filtered), color = SecondaryGrey, modifier = Modifier.padding(24.dp))
            } else {
                LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(shown, key = { it.task.id }) { t ->
                        MyTaskCard(t, onChoose = { choosing = t to it }, onMarkDone = { onMarkDone(t) },
                            onCancel = { cancelling = t }, onChat = { onChat(t) })
                    }
                }
            }
        }
        if (all.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = onPostTask,
                containerColor = OrangeSecondary, contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.post_a_task), fontWeight = FontWeight.Bold) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }

    choosing?.let { (t, offer) ->
        AlertDialog(
            onDismissRequest = { choosing = null },
            title = { Text(stringResource(R.string.mytasks_choose_title, offer.helperName)) },
            text = { Text(stringResource(R.string.mytasks_choose_body, offer.helperName, IndiaFormat.rupees(offer.amount))) },
            confirmButton = { Button(onClick = { onChoose(t, offer); choosing = null }) { Text(stringResource(R.string.mytasks_choose)) } },
            dismissButton = { TextButton(onClick = { choosing = null }) { Text(stringResource(R.string.account_cancel)) } }
        )
    }
    cancelling?.let { t ->
        AlertDialog(
            onDismissRequest = { cancelling = null },
            title = { Text(stringResource(R.string.mytasks_cancel_title)) },
            text = { Text(stringResource(R.string.mytasks_cancel_body)) },
            confirmButton = { TextButton(onClick = { onCancel(t); cancelling = null }) { Text(stringResource(R.string.mytasks_yes_cancel), color = Color(0xFFC62828)) } },
            dismissButton = { TextButton(onClick = { cancelling = null }) { Text(stringResource(R.string.mytasks_keep)) } }
        )
    }
}

@Composable
private fun EmptyTasks(onPostTask: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = BorderGrey, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.mytasks_empty_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.mytasks_empty_body), color = SecondaryGrey, textAlign = TextAlign.Center, fontSize = 15.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onPostTask, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) {
            Icon(Icons.Default.Add, contentDescription = null); Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.post_a_task), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun MyTaskCard(
    t: TaskWithOffers,
    onChoose: (Offer) -> Unit,
    onMarkDone: () -> Unit,
    onCancel: () -> Unit,
    onChat: () -> Unit,
) {
    var expanded by rememberSaveable(t.task.id) { mutableStateOf(false) }
    val chosen = t.chosen
    val status = t.task.status
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, BorderGrey)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeAgo(t.task.createdAt) ?: "", color = SecondaryGrey, fontSize = 13.sp, modifier = Modifier.weight(1f))
                when {
                    status == "completed" -> Badge(stringResource(R.string.mytasks_completed), GreenBg, GreenFg)
                    status == "cancelled" -> Badge(stringResource(R.string.mytasks_cancelled_badge), GreyBg, SecondaryGrey)
                    chosen != null -> Badge(stringResource(R.string.mytasks_helper_chosen), BlueBg, BluePrimary)
                    t.openOffers.isEmpty() -> Badge(stringResource(R.string.mytasks_no_offers), GreyBg, SecondaryGrey)
                    else -> Badge(pluralStringResource(R.plurals.offers_count, t.openOffers.size, t.openOffers.size), AmberBg, AmberFg)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(t.task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryGrey, modifier = Modifier.size(14.dp))
                Text(" " + t.task.locationQuery + " · " + stringResource(R.string.mytasks_budget,
                    IndiaFormat.budgetLabel(t.task.minBudget, t.task.maxBudget)),
                    color = SecondaryGrey, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }

            if (chosen != null) {
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.mytasks_agreed, chosen.helperName, IndiaFormat.rupees(chosen.amount)),
                    color = DarkNavy, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (status == "accepted") {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onMarkDone, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp)) {
                            Text(stringResource(R.string.mytasks_mark_done), fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = onChat, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.mytasks_chat), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (status == "submitted") {
                val offers = t.openOffers
                val visible = if (expanded) offers else offers.take(2)
                visible.forEach { offer ->
                    HorizontalDivider(color = BorderGrey, modifier = Modifier.padding(top = 10.dp))
                    Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        InitialAvatar(offer.helperName)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(offer.helperName, fontWeight = FontWeight.Bold, color = DarkNavy)
                            if (offer.message.isNotBlank()) Text(offer.message, color = SecondaryGrey, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(IndiaFormat.rupees(offer.amount), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 15.sp)
                            TextButton(onClick = { onChoose(offer) }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                                Text(stringResource(R.string.mytasks_choose), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (offers.size > 2) {
                    OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp)) {
                        Text(if (expanded) stringResource(R.string.mytasks_show_less) else pluralStringResource(R.plurals.mytasks_see_all_offers, offers.size, offers.size),
                            fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.mytasks_cancel_task), color = SecondaryGrey)
                }
            }
        }
    }
}
