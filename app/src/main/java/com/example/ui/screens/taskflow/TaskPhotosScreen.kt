package com.example.ui.screens.taskflow

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.components.BottomActionBar
import com.example.ui.components.ErrorText
import com.example.ui.components.FixhoraTextField
import com.example.ui.components.InfoCard
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SectionTitle
import com.example.ui.components.Spacing
import com.example.ui.components.StepHeader
import com.example.ui.theme.*

private const val MaxPhotos = 5
private const val TitleMaxLength = 60
private const val DetailsMaxLength = 500

@Composable
fun TaskPhotosScreen(onNext: () -> Unit, viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var titleError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MaxPhotos)
    ) { uris ->
        val existing = viewModel.uiState.value.photoUris
        uris.map { it.toString() }
            .filterNot { it in existing }
            .take(MaxPhotos - existing.size)
            .forEach { viewModel.addPhotoUri(it) }
    }
    val pickPhotos = {
        if (uiState.photoUris.size < MaxPhotos) {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    val minBudget = uiState.minBudget.toLongOrNull()
    val maxBudget = uiState.maxBudget.toLongOrNull()
    val budgetError = if (minBudget != null && maxBudget != null && minBudget > maxBudget) {
        "Maximum budget should be more than the minimum"
    } else null

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen)
        ) {
            StepHeader(
                title = "Describe your task",
                subtitle = "A clear title and a few details get you better offers"
            )

            FixhoraTextField(
                value = uiState.descriptionTitle,
                onValueChange = {
                    viewModel.updateDescription(it.take(TitleMaxLength), uiState.descriptionDetails)
                    if (it.isNotBlank()) titleError = false
                },
                label = "Task title *",
                placeholder = "e.g. Fix a leaking kitchen tap",
                errorText = if (titleError) "Add a title to continue" else null,
                counterText = "${uiState.descriptionTitle.length}/$TitleMaxLength",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            FixhoraTextField(
                value = uiState.descriptionDetails,
                onValueChange = { viewModel.updateDescription(uiState.descriptionTitle, it.take(DetailsMaxLength)) },
                label = "Details",
                placeholder = "What needs to be done, tools needed, preferred time…",
                singleLine = false,
                minLines = 4,
                maxLines = 6,
                counterText = "${uiState.descriptionDetails.length}/$DetailsMaxLength",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Spacer(modifier = Modifier.height(Spacing.md))
            SectionTitle(
                text = "Photos",
                trailing = {
                    Text(
                        "Optional · ${uiState.photoUris.size}/$MaxPhotos",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryGrey
                    )
                }
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            if (uiState.photoUris.isEmpty()) {
                UploadDropZone(onClick = pickPhotos)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    items(uiState.photoUris, key = { it }) { uriString ->
                        PhotoThumbnail(uri = uriString, onRemove = { viewModel.removePhotoUri(uriString) })
                    }
                    if (uiState.photoUris.size < MaxPhotos) {
                        item {
                            Surface(
                                onClick = pickPhotos,
                                modifier = Modifier.size(88.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MutedBackground,
                                border = BorderStroke(1.dp, LightBlueBorder)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = BluePrimary)
                                    Text("Add", style = MaterialTheme.typography.labelMedium, color = BluePrimary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            SectionTitle(
                text = "Budget",
                trailing = { Text("Optional", style = MaterialTheme.typography.bodySmall, color = SecondaryGrey) }
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FixhoraTextField(
                    value = uiState.minBudget,
                    onValueChange = { viewModel.updateBudget(it.filter(Char::isDigit).take(7), uiState.maxBudget) },
                    label = "Min",
                    prefix = "₹ ",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                )
                FixhoraTextField(
                    value = uiState.maxBudget,
                    onValueChange = { viewModel.updateBudget(uiState.minBudget, it.filter(Char::isDigit).take(7)) },
                    label = "Max",
                    prefix = "₹ ",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
            }
            if (budgetError != null) {
                ErrorText(budgetError, modifier = Modifier.padding(start = Spacing.md, top = Spacing.xxs))
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            InfoCard(
                icon = Icons.Default.Lightbulb,
                title = "Tips for better responses",
                message = "Add clear, well-lit photos, mention any tools needed and set a realistic budget so helpers can quote accurately.",
                tint = OrangeSecondary
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        BottomActionBar {
            if (titleError) {
                ErrorText("Add a task title to continue", modifier = Modifier.padding(bottom = Spacing.xs))
            }
            PrimaryButton(
                text = "Continue",
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = {
                    when {
                        uiState.descriptionTitle.isBlank() -> titleError = true
                        budgetError == null -> onNext()
                    }
                }
            )
        }
    }
}

/** Dashed upload area shown before any photo is added. */
@Composable
private fun UploadDropZone(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MutedBackground)
            .drawBehind {
                drawRoundRect(
                    color = BluePrimary.copy(alpha = 0.35f),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
                    )
                )
            }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BluePrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BluePrimary)
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text("Upload photos", style = MaterialTheme.typography.titleSmall, color = DarkNavy)
        Text("Tap to choose up to $MaxPhotos from your gallery", style = MaterialTheme.typography.bodySmall, color = SecondaryGrey)
    }
}

@Composable
private fun PhotoThumbnail(uri: String, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MutedSurface)
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Task photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(DarkNavy.copy(alpha = 0.6f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, contentDescription = "Remove photo", modifier = Modifier.size(14.dp), tint = Color.White)
        }
    }
}
