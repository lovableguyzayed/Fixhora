package com.example.ui.screens.taskflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.BentoCard
import com.example.ui.components.BottomActionBar
import com.example.ui.components.FixButton
import com.example.ui.format.Budget
import com.example.ui.format.budget
import com.example.ui.format.budgetText
import com.example.ui.format.scheduleText
import com.example.ui.screens.TaskScreen
import com.example.ui.theme.Bento
import com.example.ui.theme.BentoColors
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

@Composable
fun TaskReviewScreen(
    onSubmit: () -> Unit,
    onNavigateToStep: (String) -> Unit,
    viewModel: TaskViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val colors = FixTheme.colors
    // No fallback to the first category: falling back to "Home Repairs" told the user their task
    // was categorised when it was not.
    val category = dummyCategories.find { it.id == uiState.categoryId }

    // The success screen is reached because the task was written, not because a button was tapped.
    LaunchedEffect(submitState) {
        if (submitState == SubmitState.SUCCESS) onSubmit()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
        ) {
            StepHeader(
                title = stringResource(R.string.review_title),
                subtitle = stringResource(R.string.review_subtitle)
            )

            ReviewCard(
                icon = category?.icon ?: Icons.AutoMirrored.Filled.HelpOutline,
                bento = categoryBento(uiState.categoryId),
                label = stringResource(R.string.review_category),
                title = category?.let { stringResource(it.titleRes) } ?: stringResource(R.string.category_not_selected),
                subtitle = category?.let { stringResource(it.subtitleRes) } ?: stringResource(R.string.category_tap_edit),
                onEditClick = { onNavigateToStep(TaskScreen.Category.route) }
            )

            Spacer(modifier = Modifier.height(Spacing.md))
            // Title, description and photos are all entered on the Details step, so they share
            // one card and one Edit instead of three cards that all led to the same place.
            ReviewCard(
                icon = Icons.Default.Description,
                bento = Bento.Cream,
                label = stringResource(R.string.review_description),
                title = uiState.descriptionTitle.ifBlank { stringResource(R.string.review_no_title) },
                subtitle = uiState.descriptionDetails.ifBlank { stringResource(R.string.review_no_details) },
                onEditClick = { onNavigateToStep(TaskScreen.Photos.route) }
            ) { bento ->
                if (uiState.photoUris.isEmpty()) {
                    Text(
                        stringResource(R.string.review_no_photos),
                        style = MaterialTheme.typography.bodySmall,
                        color = bento.onFillSecondary,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                } else {
                    Row(
                        modifier = Modifier.padding(top = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        uiState.photoUris.take(3).forEachIndexed { index, uriString ->
                            AsyncImage(
                                model = uriString,
                                contentDescription = stringResource(R.string.cd_photo_n_of_m, index + 1, uiState.photoUris.size),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(Radius.sm))
                                    .background(bento.onFill.copy(alpha = 0.1f))
                            )
                        }
                    }
                    Text(
                        pluralStringResource(R.plurals.review_photos_added, uiState.photoUris.size, uiState.photoUris.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = bento.onFillSecondary,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            ReviewCard(
                icon = Icons.Default.Event,
                bento = Bento.Mint,
                label = stringResource(R.string.review_when),
                title = scheduleText(uiState.scheduledDate, uiState.timeSlot?.storageValue),
                subtitle = null,
                onEditClick = { onNavigateToStep(TaskScreen.Photos.route) }
            )

            Spacer(modifier = Modifier.height(Spacing.md))
            // "Current Location (Noida)" used to be printed here whenever the switch was on,
            // regardless of where the user actually was.
            ReviewCard(
                icon = Icons.Default.LocationOn,
                bento = Bento.Peach,
                label = stringResource(R.string.review_location),
                title = when {
                    uiState.locationQuery.isNotBlank() -> uiState.locationQuery
                    uiState.latitude != null -> stringResource(R.string.review_location_no_address)
                    else -> stringResource(R.string.review_not_specified)
                },
                subtitle = listOfNotNull(
                    listOf(uiState.addressLine.trim(), uiState.landmark.trim()).filter { it.isNotEmpty() }
                        .joinToString(", ").ifEmpty { null },
                    if (uiState.useCurrentLocation && uiState.locationQuery.isNotBlank()) {
                        stringResource(R.string.review_from_current)
                    } else {
                        null
                    }
                ).joinToString("\n").ifEmpty { null },
                onEditClick = { onNavigateToStep(TaskScreen.Location.route) }
            ) { bento ->
                Row(
                    modifier = Modifier
                        .padding(top = Spacing.sm)
                        .clip(CircleShape)
                        .background(bento.onFill.copy(alpha = 0.1f))
                        .padding(horizontal = Spacing.md, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = bento.onFill, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        stringResource(R.string.loc_within_km, uiState.selectedDistance),
                        style = MaterialTheme.typography.labelMedium,
                        color = bento.onFill
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            val hasBudget = budget(uiState.minBudget, uiState.maxBudget) != Budget.NotSet
            ReviewCard(
                icon = Icons.Default.CurrencyRupee,
                bento = Bento.Lime,
                label = stringResource(R.string.review_your_budget),
                title = budgetText(uiState.minBudget, uiState.maxBudget),
                subtitle = if (hasBudget) stringResource(R.string.review_budget_note) else null,
                onEditClick = { onNavigateToStep(TaskScreen.Photos.route) }
            )

            Spacer(modifier = Modifier.height(Spacing.lg))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.xl))
                    .background(Bento.Periwinkle.fill)
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Bento.Periwinkle.onFill,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.review_control_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = Bento.Periwinkle.onFill
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.review_control_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = Bento.Periwinkle.onFillSecondary
                    )
                }
            }

            if (submitState == SubmitState.ERROR) {
                Spacer(modifier = Modifier.height(Spacing.lg))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(colors.dangerSurface)
                        .padding(Spacing.lg),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = colors.danger,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Text(
                        stringResource(R.string.review_submit_failed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.danger,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        BottomActionBar {
            FixButton(
                text = if (submitState == SubmitState.ERROR) {
                    stringResource(R.string.review_try_again)
                } else {
                    stringResource(R.string.review_post_task)
                },
                onClick = {
                    viewModel.dismissSubmitError()
                    viewModel.submitTask()
                },
                isLoading = submitState == SubmitState.SUBMITTING,
                loadingText = stringResource(R.string.review_posting)
            )
        }
    }
}

/**
 * One summarised step on a card in that step's colour — the category's own colour for the
 * category — with a black "Edit" pill that goes back to it.
 */
@Composable
fun ReviewCard(
    icon: ImageVector,
    bento: BentoColors,
    label: String,
    title: String,
    subtitle: String?,
    onEditClick: () -> Unit,
    extraContent: @Composable (ColumnScope.(BentoColors) -> Unit)? = null
) {
    val colors = FixTheme.colors
    BentoCard(colors = bento, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.ink),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colors.onInk, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = bento.onFillSecondary,
                modifier = Modifier.weight(1f)
            )
            // A 48dp target around a small pill, so it reads as a tag but is easy to hit.
            Box(
                modifier = Modifier
                    .heightIn(min = MinTouchTarget)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onEditClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.action_edit),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onInk,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.ink)
                        .padding(horizontal = Spacing.lg, vertical = 6.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = bento.onFill)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = bento.onFillSecondary)
        }
        extraContent?.invoke(this, bento)
    }
}
