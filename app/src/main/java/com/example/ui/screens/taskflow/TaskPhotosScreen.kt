package com.example.ui.screens.taskflow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import com.example.R
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.BuildConfig

@Composable
fun TaskPhotosScreen(onNext: () -> Unit, viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isImporting by viewModel.isImportingPhotos.collectAsState()
    val remainingSlots = TaskViewModel.MAX_PHOTOS - uiState.photoUris.size

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.photos_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        
        Text(
            text = stringResource(R.string.photos_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
        )
        
        // Upload Area
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable(enabled = remainingSlots > 0 && !isImporting, role = Role.Button) {
                    launchPicker()
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, FixTheme.colors.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isImporting) {
                    CircularProgressIndicator(color = FixTheme.colors.primary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.photos_saving), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        stringResource(R.string.photos_saving_body),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FixTheme.colors.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = FixTheme.colors.primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (remainingSlots > 0) stringResource(R.string.photos_upload) else stringResource(R.string.photos_all_added, TaskViewModel.MAX_PHOTOS),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        if (remainingSlots > 0) {
                            stringResource(R.string.photos_tap_to_select, remainingSlots)
                        } else {
                            stringResource(R.string.photos_remove_one)
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Added photos section title and mock-loader helper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.photos_added), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                stringResource(R.string.photos_count, uiState.photoUris.size, TaskViewModel.MAX_PHOTOS),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(uiState.photoUris) { index, uriString ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                ) {
                    AsyncImage(
                        model = uriString,
                        // Every photo used to read "Uploaded photo", so a screen reader could not
                        // tell one thumbnail from the next — or say which one Remove would drop.
                        contentDescription = stringResource(R.string.cd_photo_n_of_m, index + 1, uiState.photoUris.size),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Remove button. The visible circle stays 20dp, but the tappable area is a
                    // full 48dp: at 20dp this was less than half the minimum touch target, which
                    // is the size a finger can actually hit. The icon also carried no
                    // contentDescription, and it is the only content of the button, so a screen
                    // reader announced the control as nothing at all.
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(MinTouchTarget)
                            .clickable(role = Role.Button) { viewModel.removePhotoUri(uriString) },
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(FixTheme.colors.surface.copy(alpha = 0.9f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_remove_photo_n, index + 1),
                                modifier = Modifier.size(14.dp),
                                tint = FixTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
            
            if (remainingSlots > 0) {
                item {
                    // Add More Button
                    OutlinedCard(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(enabled = !isImporting, role = Role.Button) { launchPicker() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            // Only content of a button, so null would announce nothing.
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.cd_add_another_photo),
                                tint = FixTheme.colors.primary
                            )
                        }
                    }
                }
            }
        }

        // Development shortcut only. This was previously visible in release builds, offering
        // real users stock photos of somebody else's plumbing as their task's evidence.
        if (BuildConfig.DEBUG && uiState.photoUris.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    viewModel.attachPhotoDirectly("https://images.unsplash.com/photo-1581094288338-2314dddb7eed?w=500&auto=format&fit=crop")
                    viewModel.attachPhotoDirectly("https://images.unsplash.com/photo-1595841696660-1e8c73d9370d?w=500&auto=format&fit=crop")
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(stringResource(R.string.photos_debug_sample), fontSize = 13.sp, color = FixTheme.colors.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // --- Task Details & Budget inputs ---
        Text(stringResource(R.string.details_title), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))

        var titleError by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = uiState.descriptionTitle,
            onValueChange = { 
                viewModel.updateDescription(it, uiState.descriptionDetails)
                if (it.isNotBlank()) titleError = false
            },
            label = { Text(stringResource(R.string.details_field_title)) },
            placeholder = { Text(stringResource(R.string.details_title_hint)) },
            isError = titleError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        )
        if (titleError) {
            Text(stringResource(R.string.details_title_required), color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.descriptionDetails,
            onValueChange = { viewModel.updateDescription(uiState.descriptionTitle, it) },
            label = { Text(stringResource(R.string.details_field_description)) },
            placeholder = { Text(stringResource(R.string.details_description_hint)) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(stringResource(R.string.budget_section_title), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.minBudget,
                onValueChange = { viewModel.updateBudget(it, uiState.maxBudget) },
                label = { Text(stringResource(R.string.budget_field_min)) },
                placeholder = { Text(stringResource(R.string.budget_hint_min)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )

            OutlinedTextField(
                value = uiState.maxBudget,
                onValueChange = { viewModel.updateBudget(uiState.minBudget, it) },
                label = { Text(stringResource(R.string.budget_field_max)) },
                placeholder = { Text(stringResource(R.string.budget_hint_max)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Tips Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = FixTheme.colors.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.tips_title), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.tips_photos), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.tips_budget), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stringResource(R.string.tips_tools), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Button(
            onClick = {
                if (uiState.descriptionTitle.isBlank()) {
                    titleError = true
                } else {
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FixTheme.colors.primary)
        ) {
            Text(
                text = stringResource(R.string.action_continue),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
