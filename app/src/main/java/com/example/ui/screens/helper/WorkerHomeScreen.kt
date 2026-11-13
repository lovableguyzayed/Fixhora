package com.example.ui.screens.helper

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.market.Task
import com.example.ui.screens.shell.*
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.screens.taskflow.title
import com.example.ui.theme.*
import com.example.util.IndiaFormat

/** Helper Home: real name, real counts, real jobs. No made-up ratings or earnings. */
@Composable
fun WorkerHomeScreen(
    name: String,
    online: Boolean,
    jobs: List<JobItem>?,
    myJobs: List<MyJob>?,
    onOnlineChange: (Boolean) -> Unit,
    onSendOffer: (Task, Long, String) -> Unit,
) {
    var offerFor by remember { mutableStateOf<Task?>(null) }
    val open = jobs.orEmpty().filter { !it.isOwn }
    val chosen = myJobs.orEmpty().count { it.offer.status == "accepted" }
    val completed = myJobs.orEmpty().count { it.offer.status == "accepted" && it.task?.status == "completed" }

    LazyColumn(
        Modifier.fillMaxSize().background(PageBg),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth().background(Color.White).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.helper_greeting), color = SecondaryGrey, fontSize = 14.sp)
                    Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkNavy, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(stringResource(if (online) R.string.helper_online else R.string.helper_offline),
                    color = if (online) GreenFg else SecondaryGrey, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Switch(checked = online, onCheckedChange = onOnlineChange,
                    colors = SwitchDefaults.colors(checkedTrackColor = GreenFg))
            }
        }
        item {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(open.size, stringResource(R.string.helper_stat_jobs_near), Color(0xFFEEF4FF), Modifier.weight(1f))
                Stat(chosen, stringResource(R.string.helper_stat_accepted), GreenBg, Modifier.weight(1f))
                Stat(completed, stringResource(R.string.helper_stat_completed), AmberBg, Modifier.weight(1f))
            }
        }
        if (completed == 0) item {
            Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.helper_ratings_note), color = SecondaryGrey, fontSize = 13.sp)
            }
        }
        item {
            Text(stringResource(R.string.helper_jobs_near_you), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp))
        }
        when {
            !online -> item { InfoText(stringResource(R.string.helper_offline_body)) }
            jobs == null -> items(2) { SkeletonCard() }
            jobs.isEmpty() -> item { InfoText(stringResource(R.string.helper_no_jobs)) }
            else -> items(jobs, key = { it.task.id }) { job ->
                JobCard(job, onSendOffer = { offerFor = job.task },
                    onQuickAccept = { amount -> onSendOffer(job.task, amount, "") })
            }
        }
    }

    offerFor?.let { task ->
        OfferSheet(task, onDismiss = { offerFor = null }, onSubmit = { amount, msg -> onSendOffer(task, amount, msg); offerFor = null })
    }
}

@Composable
private fun Stat(value: Int, label: String, bg: Color, modifier: Modifier) {
    Column(modifier.background(bg, RoundedCornerShape(14.dp)).padding(12.dp)) {
        Text(value.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
        Text(label, fontSize = 12.sp, color = SecondaryGrey, lineHeight = 15.sp)
    }
}

@Composable
private fun InfoText(text: String) {
    Text(text, color = SecondaryGrey, fontSize = 15.sp, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp))
}

@Composable
private fun SkeletonCard() {
    Surface(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(150.dp), shape = RoundedCornerShape(16.dp), color = Color.White) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(0.4f, 0.8f, 1f, 0.6f).forEach { w ->
                Box(Modifier.fillMaxWidth(w).height(14.dp).background(Color(0xFFE9ECEF), RoundedCornerShape(4.dp)))
            }
        }
    }
}

@Composable
private fun JobCard(job: JobItem, onSendOffer: () -> Unit, onQuickAccept: (Long) -> Unit) {
    val t = job.task
    val cat = dummyCategories.firstOrNull { it.id == t.categoryId }
    val posted = timeAgo(t.createdAt)
    val isNew = t.createdAt > 0 && System.currentTimeMillis() - t.createdAt < 24 * 3600_000L
    Surface(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White,
        border = BorderStroke(1.dp, BorderGrey)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (cat != null) Icon(cat.icon, contentDescription = null, tint = cat.iconTint, modifier = Modifier.size(16.dp))
                Text(" " + listOfNotNull(cat?.title, posted).joinToString(" · "), color = SecondaryGrey, fontSize = 13.sp, modifier = Modifier.weight(1f))
                if (job.isOwn) Badge(stringResource(R.string.helper_posted_by_you), GreyBg, SecondaryGrey)
                else if (isNew) Badge(stringResource(R.string.helper_badge_new), GreenBg, GreenFg)
            }
            Spacer(Modifier.height(6.dp))
            Text(t.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
            if (t.details.isNotBlank()) {
                Text(t.details, color = SecondaryGrey, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryGrey, modifier = Modifier.size(15.dp))
                Text(" " + t.locationQuery, color = SecondaryGrey, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(IndiaFormat.budgetLabel(t.minBudget, t.maxBudget), fontWeight = FontWeight.Bold, color = DarkNavy, fontSize = 15.sp)
            }
            val offer = job.myOffer
            when {
                job.isOwn -> Unit
                offer != null -> {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenFg, modifier = Modifier.size(18.dp))
                        Text(" " + stringResource(R.string.helper_offer_sent, IndiaFormat.rupees(offer.amount)), color = GreenFg,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                else -> {
                    Spacer(Modifier.height(12.dp))
                    val max = t.maxBudget.toLongOrNull() ?: t.minBudget.toLongOrNull()
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onSendOffer, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BluePrimary)) {
                            Text(stringResource(R.string.helper_send_offer), fontWeight = FontWeight.Bold, color = BluePrimary)
                        }
                        if (max != null) Button(onClick = { onQuickAccept(max) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) {
                            Text(stringResource(R.string.helper_accept_for, IndiaFormat.rupees(max)), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfferSheet(task: Task, onDismiss: () -> Unit, onSubmit: (Long, String) -> Unit) {
    var amount by rememberSaveable { mutableStateOf(task.maxBudget.ifBlank { task.minBudget }) }
    var message by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    val value = amount.toLongOrNull()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).imePadding()) {
            Text(stringResource(R.string.helper_offer_title), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            Text(task.title + " · " + IndiaFormat.budgetLabel(task.minBudget, task.maxBudget), color = SecondaryGrey, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = amount, onValueChange = { amount = IndiaFormat.cleanAmount(it); showError = false },
                label = { Text(stringResource(R.string.helper_offer_amount)) }, singleLine = true, isError = showError,
                supportingText = { if (showError) Text(stringResource(R.string.helper_offer_amount_error)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = message, onValueChange = { message = it.take(200) },
                label = { Text(stringResource(R.string.helper_offer_message)) },
                placeholder = { Text(stringResource(R.string.helper_offer_message_hint)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp)
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { if (value != null && value > 0) onSubmit(value, message) else showError = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(if (value != null && value > 0) stringResource(R.string.helper_offer_submit, IndiaFormat.rupees(value))
                    else stringResource(R.string.helper_send_offer), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

/** Offers this helper has sent and what happened to them. */
@Composable
fun MyJobsScreen(myJobs: List<MyJob>?, onExploreJobs: () -> Unit, onChat: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(PageBg)) {
        Text(stringResource(R.string.nav_my_jobs), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkNavy,
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp))
        when {
            myJobs == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            myJobs.isEmpty() -> Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = BorderGrey, modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.myjobs_empty), color = SecondaryGrey, fontSize = 15.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onExploreJobs, colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) {
                    Text(stringResource(R.string.myjobs_explore))
                }
            }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(myJobs, key = { it.offer.id }) { j ->
                    val task = j.task
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, BorderGrey)) {
                        Column(Modifier.padding(16.dp).fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(timeAgo(j.offer.createdAt) ?: "", color = SecondaryGrey, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                when {
                                    j.offer.status == "accepted" && task?.status == "completed" -> Badge(stringResource(R.string.myjobs_done), GreenBg, GreenFg)
                                    j.offer.status == "accepted" -> Badge(stringResource(R.string.myjobs_chosen), BlueBg, BluePrimary)
                                    j.offer.status == "declined" || task?.status == "cancelled" -> Badge(stringResource(R.string.myjobs_not_chosen), GreyBg, SecondaryGrey)
                                    else -> Badge(stringResource(R.string.myjobs_waiting), AmberBg, AmberFg)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(task?.title ?: "—", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                            task?.let { Text(it.locationQuery, color = SecondaryGrey, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            Text(stringResource(R.string.myjobs_your_offer, IndiaFormat.rupees(j.offer.amount)), color = DarkNavy,
                                fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 6.dp))
                            if (j.offer.status == "accepted" && task?.status == "accepted") {
                                Button(onClick = { onChat(j.offer.taskId) }, modifier = Modifier.padding(top = 10.dp).fillMaxWidth().heightIn(min = 48.dp),
                                    shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) {
                                    Text(stringResource(R.string.mytasks_chat), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
