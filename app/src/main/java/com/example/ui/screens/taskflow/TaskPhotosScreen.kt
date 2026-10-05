package com.example.ui.screens.taskflow

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.BottomActionBar
import com.example.ui.components.FixButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

/** Budgets are whole rupees; seven digits is already ₹99,99,999, far past any household job. */
private const val BUDGET_MAX_DIGITS = 7

/**
 * The "Details" step: what the job is, optional photos, and an optional budget.
 *
 * The required title now comes first. It used to sit below an optional photo picker, so the one
 * field that blocks posting was the one most likely to be scrolled past.
 */
@Composable
fun TaskPhotosScreen(onNext: () -> Unit, viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isImporting by viewModel.isImportingPhotos.collectAsState()
    val remainingSlots = TaskViewModel.MAX_PHOTOS - uiState.photoUris.size
    val colors = FixTheme.colors

    var titleError by remember { mutableStateOf(false) }
    val minValue = uiState.minBudget.toLongOrNull()
    val maxValue = uiState.maxBudget.toLongOrNull()
    val budgetInverted = minValue != null && maxValue != null && maxValue < minValue

    // The picker only grants read access until the app stops, so the ViewModel copies each photo
    // into app storage rather than storing the picker's URI.
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = TaskViewModel.MAX_PHOTOS
        )
    ) { uris -> viewModel.importPhotos(uris) }

    fun launchPicker() {
        if (remainingSlots > 0 && !isImporting) {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
        ) {
            StepHeader(
                title = stringResource(R.string.details_title),
                subtitle = stringResource(R.string.details_subtitle)
            )

            StepTextField(
                value = uiState.descriptionTitle,
                onValueChange = {
                    viewModel.updateDescription(it, uiState.descriptionDetails)
                    if (it.isNotBlank()) titleError = false
                },
                label = stringResource(R.string.details_field_title),
                placeholder = stringResource(R.string.details_title_hint),
                isError = titleError,
                supportingText = if (titleError) stringResource(R.string.details_title_required) else null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            StepTextField(
                value = uiState.descriptionDetails,
                onValueChange = { viewModel.updateDescription(uiState.descriptionTitle, it) },
                label = stringResource(R.string.details_field_description),
                placeholder = stringResource(R.string.details_description_hint),
                singleLine = false,
                minLines = 4,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
            SectionHeader(
                title = stringResource(R.string.photos_title),
                trailing = if (uiState.photoUris.isNotEmpty()) {
                    {
                        Text(
                            text = stringResource(R.string.photos_count, uiState.photoUris.size, TaskViewModel.MAX_PHOTOS),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textSecondary
                        )
                    }
                } else null
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = stringResource(R.string.photos_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            if (uiState.photoUris.isEmpty()) {
                UploadZone(isImporting = isImporting, onClick = ::launchPicker)
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    itemsIndexed(uiState.photoUris, key = { _, uri -> uri }) { index, uriString ->
                        PhotoThumbnail(
                            uri = uriString,
                            index = index,
                            total = uiState.photoUris.size,
                            onRemove = { viewModel.removePhotoUri(uriString) }
                        )
                    }
                    if (remainingSlots > 0 || isImporting) {
                        item {
                            AddPhotoTile(isImporting = isImporting, onClick = ::launchPicker)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = if (remainingSlots > 0) {
                        stringResource(R.string.photos_tap_to_select, remainingSlots)
                    } else {
                        stringResource(R.string.photos_all_added, TaskViewModel.MAX_PHOTOS) + " · " +
                            stringResource(R.string.photos_remove_one)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            SectionHeader(title = stringResource(R.string.budget_section_title))
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                StepTextField(
                    value = uiState.minBudget,
                    onValueChange = {
                        viewModel.updateBudget(it.filter(Char::isDigit).take(BUDGET_MAX_DIGITS), uiState.maxBudget)
                    },
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.budget_field_min),
                    placeholder = stringResource(R.string.budget_hint_min),
                    prefix = "₹",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                )
                StepTextField(
                    value = uiState.maxBudget,
                    onValueChange = {
                        viewModel.updateBudget(uiState.minBudget, it.filter(Char::isDigit).take(BUDGET_MAX_DIGITS))
                    },
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.budget_field_max),
                    placeholder = stringResource(R.string.budget_hint_max),
                    prefix = "₹",
                    isError = budgetInverted,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
            }
            if (budgetInverted) {
                Text(
                    text = stringResource(R.string.budget_max_below_min),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = Spacing.lg, top = Spacing.xs)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            TipsCard()
            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        BottomActionBar {
            FixButton(
                text = stringResource(R.string.action_continue),
                onClick = {
                    titleError = uiState.descriptionTitle.isBlank()
                    if (!titleError && !budgetInverted) onNext()
                }
            )
        }
    }
}

/** A dashed outline, the usual signal for "drop or pick files here". */
private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp): Modifier = drawBehind {
    val stroke = 1.5.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
        size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())))
    )
}

@Composable
private fun UploadZone(isImporting: Boolean, onClick: () -> Unit) {
    val colors = FixTheme.colors
    val shape = RoundedCornerShape(Radius.lg)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 148.dp)
            .clip(shape)
            .background(colors.primarySurface.copy(alpha = 0.5f))
            .dashedBorder(colors.primary.copy(alpha = 0.6f), Radius.lg)
            .clickable(enabled = !isImporting, role = Role.Button, onClick = onClick)
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isImporting) {
            CircularProgressIndicator(color = colors.primary, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                stringResource(R.string.photos_saving),
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                stringResource(R.string.photos_saving_body),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = colors.primary)
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                stringResource(R.string.photos_upload),
                style = MaterialTheme.typography.titleSmall,
                color = colors.primary
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                stringResource(R.string.photos_tap_to_select, TaskViewModel.MAX_PHOTOS),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private val ThumbnailSize = 88.dp

@Composable
private fun PhotoThumbnail(uri: String, index: Int, total: Int, onRemove: () -> Unit) {
    val colors = FixTheme.colors
    Box(
        modifier = Modifier
            .size(ThumbnailSize)
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surfaceAlt)
    ) {
        AsyncImage(
            model = uri,
            // Every photo used to read "Uploaded photo", so a screen reader could not tell one
            // thumbnail from the next — or say which one Remove would drop.
            contentDescription = stringResource(R.string.cd_photo_n_of_m, index + 1, total),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // The visible circle is small, but the tappable area is the full 48dp minimum touch target.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(MinTouchTarget)
                .clickable(role = Role.Button, onClick = onRemove),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .padding(Spacing.xs)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_remove_photo_n, index + 1),
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun AddPhotoTile(isImporting: Boolean, onClick: () -> Unit) {
    val colors = FixTheme.colors
    Box(
        modifier = Modifier
            .size(ThumbnailSize)
            .clip(RoundedCornerShape(Radius.md))
            .dashedBorder(colors.primary.copy(alpha = 0.6f), Radius.md)
            .clickable(enabled = !isImporting, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isImporting) {
            CircularProgressIndicator(
                color = colors.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            // Only content of a button, so null would announce nothing.
            Icon(
                Icons.Default.AddPhotoAlternate,
                contentDescription = stringResource(R.string.cd_add_another_photo),
                tint = colors.primary
            )
        }
    }
}

@Composable
private fun TipsCard() {
    val colors = FixTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.accentSurface)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = colors.accentText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.tips_title),
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            listOf(R.string.tips_photos, R.string.tips_budget, R.string.tips_tools).forEach { tip ->
                Text(
                    stringResource(tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
            }
        }
    }
}
