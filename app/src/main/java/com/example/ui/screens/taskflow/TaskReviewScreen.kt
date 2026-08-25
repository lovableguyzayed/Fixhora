package com.example.ui.screens.taskflow

import com.example.R
import com.example.ui.screens.TaskScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FixCard
import com.example.ui.theme.FixTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun TaskReviewScreen(
    onSubmit: () -> Unit,
    onNavigateToStep: (String) -> Unit,
    viewModel: TaskViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val scrollState = rememberScrollState()
    // No fallback to the first category: falling back to "Home Repairs" told the user their task
    // was categorised when it was not.
    val category = dummyCategories.find { it.id == uiState.categoryId }

    // The success screen is reached because the task was written, not because a button was tapped.
    LaunchedEffect(submitState) {
        if (submitState == SubmitState.SUCCESS) onSubmit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = stringResource(R.string.review_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        
        Text(
            text = stringResource(R.string.review_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
        )
        
        // Category Card
        ReviewCard(
            label = stringResource(R.string.review_category),
            title = category?.let { stringResource(it.titleRes) } ?: stringResource(R.string.category_not_selected),
            subtitle = category?.let { stringResource(it.subtitleRes) } ?: stringResource(R.string.category_tap_edit),
            onEditClick = { onNavigateToStep(TaskScreen.Category.route) },
            icon = {
                val tint = category?.iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(tint.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(
                        category?.icon ?: Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = tint
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Description Card
        ReviewCard(
            label = stringResource(R.string.review_description),
            title = uiState.descriptionTitle.ifEmpty { stringResource(R.string.review_no_title) },
            subtitle = uiState.descriptionDetails.ifEmpty { stringResource(R.string.review_no_details) },
            onEditClick = { onNavigateToStep(TaskScreen.Photos.route) },
            icon = {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(FixTheme.colors.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = FixTheme.colors.primary)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Location Card
        // "Current Location (Noida)" used to be printed here whenever the switch was on,
        // regardless of where the user actually was.
        ReviewCard(
            label = stringResource(R.string.review_location),
            title = when {
                uiState.locationQuery.isNotBlank() -> uiState.locationQuery
                uiState.latitude != null -> stringResource(R.string.review_location_no_address)
                else -> stringResource(R.string.review_not_specified)
            },
            subtitle = if (uiState.useCurrentLocation && uiState.locationQuery.isNotBlank()) {
                stringResource(R.string.review_from_current)
            } else {
                null
            },
            onEditClick = { onNavigateToStep(TaskScreen.Location.route) },
            extraContent = {
                Surface(
                    color = FixTheme.colors.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = FixTheme.colors.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.loc_within_km, uiState.selectedDistance), color = FixTheme.colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            icon = {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(FixTheme.colors.primarySurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = FixTheme.colors.primary)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Photos Card
        ReviewCard(
            label = stringResource(R.string.review_photos),
            title = if (uiState.photoUris.isEmpty()) stringResource(R.string.review_no_photos) else stringResource(R.string.review_photos_added, uiState.photoUris.size),
            subtitle = null,
            onEditClick = { onNavigateToStep(TaskScreen.Photos.route) },
            extraContent = {
                if (uiState.photoUris.isNotEmpty()) {
                    Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.photoUris.take(4).forEach { uriString ->
                            AsyncImage(
                                model = uriString,
                                contentDescription = stringResource(R.string.cd_review_image),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FixTheme.colors.border)
                            )
                        }
                    }
                }
            },
            icon = {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(FixTheme.colors.primarySurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = FixTheme.colors.primary)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Budget Card
        val budgetTitle = if (uiState.minBudget.isNotBlank() && uiState.maxBudget.isNotBlank()) {
            stringResource(R.string.review_budget_range, uiState.minBudget, uiState.maxBudget)
        } else if (uiState.minBudget.isNotBlank()) {
            stringResource(R.string.review_budget_min, uiState.minBudget)
        } else if (uiState.maxBudget.isNotBlank()) {
            stringResource(R.string.review_budget_max, uiState.maxBudget)
        } else {
            stringResource(R.string.review_no_budget)
        }
        
        ReviewCard(
            label = stringResource(R.string.review_your_budget),
            title = budgetTitle,
            subtitle = stringResource(R.string.review_budget_note),
            onEditClick = { onNavigateToStep(TaskScreen.Photos.route) },
            icon = {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(FixTheme.colors.primarySurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = FixTheme.colors.primary)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Safety Guarantee
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = FixTheme.colors.primary.copy(alpha = 0.05f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = FixTheme.colors.primary,
                    modifier = Modifier.padding(end = 16.dp).size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.review_control_title), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(stringResource(R.string.review_control_body), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        if (submitState == SubmitState.ERROR) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        stringResource(R.string.review_submit_failed),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp
                    )
                }
            }
        }

        val isSubmitting = submitState == SubmitState.SUBMITTING
        Button(
            onClick = {
                viewModel.dismissSubmitError()
                viewModel.submitTask()
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FixTheme.colors.primary)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = FixTheme.colors.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.review_posting), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(
                    text = if (submitState == SubmitState.ERROR) stringResource(R.string.review_try_again) else stringResource(R.string.review_post_task),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
        
        Row(
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.review_secure), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ReviewCard(
    label: String,
    title: String,
    subtitle: String?,
    onEditClick: () -> Unit,
    icon: @Composable () -> Unit,
    extraContent: @Composable (() -> Unit)? = null
) {
    FixCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (extraContent != null) {
                    extraContent()
                }
            }
            TextButton(onClick = onEditClick, contentPadding = PaddingValues(0.dp)) {
                Text(stringResource(R.string.action_edit), color = FixTheme.colors.primary, fontWeight = FontWeight.Medium)
            }
        }
    }
}
