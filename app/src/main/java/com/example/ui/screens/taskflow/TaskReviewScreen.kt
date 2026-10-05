package com.example.ui.screens.taskflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.components.BottomActionBar
import com.example.ui.components.FixhoraCard
import com.example.ui.components.IconBadge
import com.example.ui.components.InfoCard
import com.example.ui.components.PrimaryButton
import com.example.ui.components.Spacing
import com.example.ui.components.StatusChip
import com.example.ui.components.StepHeader
import com.example.ui.components.formatBudget
import com.example.ui.screens.TaskScreen
import com.example.ui.theme.*

@Composable
fun TaskReviewScreen(
    onSubmit: () -> Unit,
    onNavigateToStep: (String) -> Unit,
    viewModel: TaskViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val category = categoryFor(uiState.categoryId) ?: dummyCategories.last()
    val budget = formatBudget(uiState.minBudget, uiState.maxBudget)
    val photoCount = uiState.photoUris.size

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            StepHeader(title = "Review your task", subtitle = "Check the details before posting")

            ReviewCard(
                label = "Category",
                title = category.title,
                subtitle = category.subtitle,
                onEditClick = { onNavigateToStep(TaskScreen.Category.route) },
                icon = category.icon,
                iconTint = category.iconTint
            )

            ReviewCard(
                label = "Task",
                title = uiState.descriptionTitle.ifBlank { "Help with ${category.title.lowercase()}" },
                subtitle = uiState.descriptionDetails.ifBlank { "No extra details added" },
                onEditClick = { onNavigateToStep(TaskScreen.Details.route) },
                icon = Icons.Default.Description
            )

            ReviewCard(
                label = "Location",
                title = if (uiState.useCurrentLocation) "Current location" else uiState.locationQuery.ifBlank { "Not specified" },
                subtitle = if (uiState.useCurrentLocation) CurrentLocationAddress else null,
                onEditClick = { onNavigateToStep(TaskScreen.Location.route) },
                icon = Icons.Default.LocationOn,
                extraContent = {
                    Row(modifier = Modifier.padding(top = Spacing.xs)) {
                        StatusChip(text = "Within ${uiState.selectedDistance} km", color = BluePrimary)
                    }
                }
            )

            ReviewCard(
                label = "Photos",
                title = when (photoCount) {
                    0 -> "No photos added"
                    1 -> "1 photo added"
                    else -> "$photoCount photos added"
                },
                subtitle = null,
                onEditClick = { onNavigateToStep(TaskScreen.Details.route) },
                icon = Icons.Default.Image,
                extraContent = if (photoCount > 0) {
                    {
                        Row(
                            modifier = Modifier.padding(top = Spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            uiState.photoUris.take(4).forEach { uriString ->
                                AsyncImage(
                                    model = uriString,
                                    contentDescription = "Task photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MutedSurface)
                                )
                            }
                        }
                    }
                } else null
            )

            ReviewCard(
                label = "Budget",
                title = budget ?: "Open to offers",
                subtitle = if (budget != null) "Helpers will quote within this range" else "Helpers will send you their own quotes",
                onEditClick = { onNavigateToStep(TaskScreen.Details.route) },
                icon = Icons.Default.CurrencyRupee
            )

            InfoCard(
                icon = Icons.Default.VerifiedUser,
                title = "You're in control",
                message = "Compare offers from nearby helpers and choose the one that suits you best.",
                modifier = Modifier.padding(top = Spacing.xxs)
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        BottomActionBar {
            PrimaryButton(
                text = "Post Task",
                onClick = {
                    viewModel.submitTask()
                    onSubmit()
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = HintGrey)
                Spacer(modifier = Modifier.width(Spacing.xxs))
                Text("Your details are safe and secure", style = MaterialTheme.typography.bodySmall, color = HintGrey)
            }
        }
    }
}

@Composable
fun ReviewCard(
    label: String,
    title: String,
    subtitle: String?,
    onEditClick: () -> Unit,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color = BluePrimary,
    extraContent: (@Composable () -> Unit)? = null
) {
    FixhoraCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = Spacing.md, top = Spacing.md, bottom = Spacing.md, end = Spacing.xxs),
            verticalAlignment = Alignment.Top
        ) {
            IconBadge(icon = icon, tint = iconTint, size = 40.dp, iconSize = 22.dp)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f).padding(top = 2.dp)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = SecondaryGrey)
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkNavy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryGrey,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                extraContent?.invoke()
            }
            TextButton(
                onClick = onEditClick,
                contentPadding = PaddingValues(horizontal = Spacing.sm),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = BluePrimary)
                Spacer(modifier = Modifier.width(Spacing.xxs))
                Text("Edit", color = BluePrimary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
