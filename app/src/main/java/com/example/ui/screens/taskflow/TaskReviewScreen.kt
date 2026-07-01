package com.example.ui.screens.taskflow

import com.example.ui.screens.TaskScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary

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
    val scrollState = rememberScrollState()
    val category = dummyCategories.find { it.id == uiState.categoryId } ?: dummyCategories.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Review your task",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        
        Text(
            text = "Please review all details before posting",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
        )
        
        // Category Card
        ReviewCard(
            label = "Category",
            title = category.title,
            subtitle = category.subtitle,
            onEditClick = { onNavigateToStep(TaskScreen.Category.route) },
            icon = {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(category.iconTint.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(category.icon, contentDescription = null, tint = category.iconTint)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Description Card
        ReviewCard(
            label = "Task Description",
            title = uiState.descriptionTitle.ifEmpty { "Need Help with ${category.title}" },
            subtitle = uiState.descriptionDetails.ifEmpty { "Looking for someone to help me out." },
            onEditClick = { onNavigateToStep(TaskScreen.Photos.route) },
            icon = {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(BluePrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = BluePrimary)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Location Card
        ReviewCard(
            label = "Location",
            title = if (uiState.useCurrentLocation) "Current Location (Noida)" else uiState.locationQuery.ifEmpty { "Not specified" },
            subtitle = null,
            onEditClick = { onNavigateToStep(TaskScreen.Location.route) },
            extraContent = {
                Surface(
                    color = BluePrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Within ${uiState.selectedDistance} km", color = BluePrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            icon = {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE8F0FE)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BluePrimary)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Photos Card
        ReviewCard(
            label = "Photos",
            title = if (uiState.photoUris.isEmpty()) "No photos added" else "${uiState.photoUris.size} photos added",
            subtitle = null,
            onEditClick = { onNavigateToStep(TaskScreen.Photos.route) },
            extraContent = {
                if (uiState.photoUris.isNotEmpty()) {
                    Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.photoUris.take(4).forEach { uriString ->
                            AsyncImage(
                                model = uriString,
                                contentDescription = "Review image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                            )
                        }
                    }
                }
            },
            icon = {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE8F0FE)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = BluePrimary)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Budget Card
        val budgetTitle = if (uiState.minBudget.isNotBlank() && uiState.maxBudget.isNotBlank()) {
            "₹${uiState.minBudget} - ₹${uiState.maxBudget}"
        } else if (uiState.minBudget.isNotBlank()) {
            "Min ₹${uiState.minBudget}"
        } else if (uiState.maxBudget.isNotBlank()) {
            "Max ₹${uiState.maxBudget}"
        } else {
            "No budget specified"
        }
        
        ReviewCard(
            label = "Your Budget",
            title = budgetTitle,
            subtitle = "Helpers will submit their offers within this range",
            onEditClick = { onNavigateToStep(TaskScreen.Photos.route) },
            icon = {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE8F0FE)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = BluePrimary)
                }
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Safety Guarantee
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BluePrimary.copy(alpha = 0.05f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.padding(end = 16.dp).size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("You're in control", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("You'll receive offers from nearby helpers and choose the best one for your task.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Button(
            onClick = {
                viewModel.submitTask()
                onSubmit()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text(
                text = "Post Task",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
        
        Row(
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Your details are safe and secure", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
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
                Text("Edit", color = BluePrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}
