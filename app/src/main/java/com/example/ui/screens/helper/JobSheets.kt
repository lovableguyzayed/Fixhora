package com.example.ui.screens.helper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.room.TaskEntity
import com.example.ui.components.CompactButton
import com.example.ui.components.FixhoraCard
import com.example.ui.components.FixhoraTextField
import com.example.ui.components.IconBadge
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.MetaItem
import com.example.ui.components.MockPeople
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.Spacing
import com.example.ui.components.StatusChip
import com.example.ui.components.areaLabel
import com.example.ui.components.budgetLabel
import com.example.ui.components.formatRupees
import com.example.ui.components.relativeTime
import com.example.ui.screens.taskflow.categoryFor
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*

/** Label + colour for each task status, so every screen names statuses the same way. */
data class StatusStyle(val label: String, val color: Color)

fun statusStyle(status: String): StatusStyle = when (status) {
    "submitted" -> StatusStyle("New", SuccessGreen)
    "pending" -> StatusStyle("Bid sent", WarningAmber)
    "accepted" -> StatusStyle("Active", BluePrimary)
    "completed" -> StatusStyle("Completed", SecondaryGrey)
    "rejected" -> StatusStyle("Declined", ErrorRed)
    else -> StatusStyle("Draft", HintGrey)
}

sealed interface JobSheet {
    val task: TaskEntity
    data class Details(override val task: TaskEntity) : JobSheet
    data class Bid(override val task: TaskEntity) : JobSheet
}

/** Hosts the job-details and place-bid bottom sheets used by the Home and Map tabs. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSheetHost(
    sheet: JobSheet?,
    onSheetChange: (JobSheet?) -> Unit,
    viewModel: HelperViewModel,
    onOpenChat: (Int) -> Unit,
) {
    when (sheet) {
        is JobSheet.Details -> ModalBottomSheet(
            onDismissRequest = { onSheetChange(null) },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGrey) }
        ) {
            JobDetailsContent(
                task = sheet.task,
                onPlaceBidClick = { onSheetChange(JobSheet.Bid(sheet.task)) },
                onAcceptClick = {
                    viewModel.acceptTask(sheet.task)
                    onSheetChange(null)
                },
                onChatClick = {
                    onSheetChange(null)
                    onOpenChat(sheet.task.id)
                }
            )
        }
        is JobSheet.Bid -> ModalBottomSheet(
            onDismissRequest = { onSheetChange(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGrey) }
        ) {
            PlaceBidContent(
                task = sheet.task,
                onSubmit = {
                    viewModel.placeBid(sheet.task)
                    onSheetChange(null)
                },
                onCancel = { onSheetChange(null) }
            )
        }
        null -> Unit
    }
}

/** Feed card for an open job. Tapping the card opens its details. */
@Composable
fun WorkerJobCard(
    task: TaskEntity,
    onClick: () -> Unit,
    onPlaceBidClick: () -> Unit,
    onAccept: () -> Unit,
) {
    val category = categoryFor(task.categoryId) ?: dummyCategories.last()
    val customer = MockPeople.customerName(task.id)

    FixhoraCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            CustomerRow(task = task, customer = customer) {
                StatusChip(text = category.title, color = category.iconTint)
            }

            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = task.descriptionTitle.ifBlank { "Help with ${category.title.lowercase()}" },
                style = MaterialTheme.typography.titleMedium,
                color = DarkNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(
                text = task.descriptionDetails.ifBlank { "No extra details added." },
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryGrey,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                MetaItem(Icons.Default.LocationOn, "${task.areaLabel()} · ${MockPeople.distanceKm(task.id)}", modifier = Modifier.weight(1f, fill = false))
                MetaItem(Icons.Default.AccessTime, relativeTime(task.createdAt))
            }

            HorizontalDivider(color = DividerGrey, modifier = Modifier.padding(vertical = Spacing.sm))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Budget", style = MaterialTheme.typography.bodySmall, color = SecondaryGrey)
                    Text(
                        task.budgetLabel(),
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                CompactButton(text = "Bid", onClick = onPlaceBidClick, outlined = true)
                Spacer(modifier = Modifier.width(Spacing.xs))
                CompactButton(text = "Accept", onClick = onAccept)
            }
        }
    }
}

/** Avatar, name, verified tick, rating and post time, with an optional trailing slot. */
@Composable
fun CustomerRow(task: TaskEntity, customer: String, trailing: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        InitialsAvatar(name = customer, size = 40.dp)
        Spacer(modifier = Modifier.width(Spacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    customer,
                    style = MaterialTheme.typography.titleSmall,
                    color = DarkNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = BluePrimary, modifier = Modifier.size(14.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = RatingGold, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    "${MockPeople.rating(task.id)} · ${relativeTime(task.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryGrey
                )
            }
        }
        Spacer(modifier = Modifier.width(Spacing.xs))
        trailing()
    }
}

@Composable
fun JobDetailsContent(
    task: TaskEntity,
    onPlaceBidClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val category = categoryFor(task.categoryId) ?: dummyCategories.last()
    val customer = MockPeople.customerName(task.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen)
            .padding(bottom = Spacing.lg)
    ) {
        CustomerRow(task = task, customer = customer) {
            StatusChip(text = category.title, color = category.iconTint)
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(
            text = task.descriptionTitle.ifBlank { "Help with ${category.title.lowercase()}" },
            style = MaterialTheme.typography.titleLarge,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = task.descriptionDetails.ifBlank { "No extra details added." },
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryGrey
        )

        Spacer(modifier = Modifier.height(Spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            JobDetailItem(Icons.Default.LocationOn, "Distance", MockPeople.distanceKm(task.id), Modifier.weight(1f))
            JobDetailItem(Icons.Default.AccessTime, "Posted", relativeTime(task.createdAt), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        FixhoraCard(modifier = Modifier.fillMaxWidth(), containerColor = MutedBackground, borderColor = MutedBackground) {
            Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Customer's budget", style = MaterialTheme.typography.bodySmall, color = SecondaryGrey)
                    Text(task.budgetLabel(), style = MaterialTheme.typography.titleLarge, color = DarkNavy)
                }
                Text(task.areaLabel(), style = MaterialTheme.typography.bodySmall, color = SecondaryGrey)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SecondaryButton(text = "Place Bid", onClick = onPlaceBidClick, modifier = Modifier.weight(1f))
            PrimaryButton(text = "Accept Job", onClick = onAcceptClick, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        TextButton(
            onClick = onChatClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .heightIn(min = 48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp), tint = BluePrimary)
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text("Chat with $customer", style = MaterialTheme.typography.titleSmall, color = BluePrimary)
        }
    }
}

@Composable
private fun JobDetailItem(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
    FixhoraCard(modifier = modifier) {
        Row(modifier = Modifier.padding(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, tint = BluePrimary, size = 36.dp, iconSize = 18.dp)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Column {
                Text(title, style = MaterialTheme.typography.bodySmall, color = SecondaryGrey)
                Text(value, style = MaterialTheme.typography.titleSmall, color = DarkNavy)
            }
        }
    }
}

@Composable
fun PlaceBidContent(task: TaskEntity, onSubmit: () -> Unit, onCancel: () -> Unit) {
    var bidAmount by remember { mutableStateOf(task.maxBudget.ifBlank { task.minBudget }) }
    var estimatedTime by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val amountError = if (submitted && bidAmount.isBlank()) "Enter your bid amount" else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen)
            .padding(bottom = Spacing.lg)
    ) {
        Text("Place a bid", style = MaterialTheme.typography.headlineSmall, color = DarkNavy)
        Spacer(modifier = Modifier.height(Spacing.xxs))
        Text(
            "Customer's budget: ${task.budgetLabel()}",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryGrey
        )

        Spacer(modifier = Modifier.height(Spacing.lg))
        FixhoraTextField(
            value = bidAmount,
            onValueChange = { bidAmount = it.filter(Char::isDigit).take(7) },
            label = "Your bid",
            prefix = "₹ ",
            errorText = amountError,
            helperText = bidAmount.toLongOrNull()?.let { "You'll quote ${formatRupees(it.toString())}" },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        FixhoraTextField(
            value = estimatedTime,
            onValueChange = { estimatedTime = it },
            label = "When can you do it?",
            placeholder = "e.g. Today, 5 PM",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        FixhoraTextField(
            value = message,
            onValueChange = { message = it.take(300) },
            label = "Message to customer (optional)",
            placeholder = "Introduce yourself and your experience",
            singleLine = false,
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Spacer(modifier = Modifier.height(Spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                contentColor = DarkNavy,
                borderColor = BorderGrey
            )
            PrimaryButton(
                text = "Submit Bid",
                onClick = {
                    submitted = true
                    if (bidAmount.isNotBlank()) onSubmit()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
