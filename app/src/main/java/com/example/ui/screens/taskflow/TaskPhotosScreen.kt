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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FixTheme

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
            text = "Add photos (optional)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        
        Text(
            text = "Add photos to help helpers understand the task better.",
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
                .clickable(enabled = remainingSlots > 0 && !isImporting) { launchPicker() },
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
                    Text("Saving photos…", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        "Copying them into the app so they stay with your task.",
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
                        if (remainingSlots > 0) "Upload photos" else "All ${TaskViewModel.MAX_PHOTOS} photos added",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        if (remainingSlots > 0) {
                            "Tap to select up to $remainingSlots more from your gallery"
                        } else {
                            "Remove one to add a different photo"
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
            Text("Added photos", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                "${uiState.photoUris.size}/${TaskViewModel.MAX_PHOTOS}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.photoUris) { uriString ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                ) {
                    AsyncImage(
                        model = uriString,
                        contentDescription = "Uploaded photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Close button overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(FixTheme.colors.surface.copy(alpha = 0.9f))
                            .clickable { viewModel.removePhotoUri(uriString) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = FixTheme.colors.textPrimary)
                    }
                }
            }
            
            if (remainingSlots > 0) {
                item {
                    // Add More Button
                    OutlinedCard(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(enabled = !isImporting) { launchPicker() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = FixTheme.colors.primary)
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
                Text("⚡ Debug: attach sample photos", fontSize = 13.sp, color = FixTheme.colors.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // --- Task Details & Budget inputs ---
        Text("Task Details", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))

        var titleError by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = uiState.descriptionTitle,
            onValueChange = { 
                viewModel.updateDescription(it, uiState.descriptionDetails)
                if (it.isNotBlank()) titleError = false
            },
            label = { Text("Task Title *") },
            placeholder = { Text("e.g. Clean my kitchen sink / fix wood drawer") },
            isError = titleError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        )
        if (titleError) {
            Text("Title is required to post the task", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.descriptionDetails,
            onValueChange = { viewModel.updateDescription(uiState.descriptionTitle, it) },
            label = { Text("Detailed Description") },
            placeholder = { Text("Provide details like what tools are needed, special requests, size of work etc.") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text("Set Budget Range (Optional)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.minBudget,
                onValueChange = { viewModel.updateBudget(it, uiState.maxBudget) },
                label = { Text("Min Budget (₹)") },
                placeholder = { Text("e.g. 500") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )

            OutlinedTextField(
                value = uiState.maxBudget,
                onValueChange = { viewModel.updateBudget(uiState.minBudget, it) },
                label = { Text("Max Budget (₹)") },
                placeholder = { Text("e.g. 1500") },
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
                    Text("Tips for better responses", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Add clear, well-lit photos", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Provide an accurate budget range so helpers can bid effectively", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Detail any special equipment/tools required", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
