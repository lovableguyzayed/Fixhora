package com.example.ui.screens.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.format.posterText
import com.example.ui.components.EmptyState
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*

/**
 * Browse open jobs by category.
 *
 * This was a fake map: markers were placed at fixed screen offsets with `index * 20.dp` spacing,
 * each labelled with a budget or the word "Bid", and every one flagged urgent by
 * `val isUrgent = true // Mock logic`. None of that reflected where anything actually was. There
 * is no maps SDK in this build, so the screen now does the thing it can genuinely do — filter the
 * open jobs — and says plainly that the map itself is not here yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerMapScreen(viewModel: HelperViewModel, onOpenChat: (Int) -> Unit) {
    val tasks by viewModel.mapTasks.collectAsState()
    val query by viewModel.mapQuery.collectAsState()
    val selectedCategoryId by viewModel.mapCategoryId.collectAsState()
    val ownerNames by viewModel.ownerNames.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title), fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.surface)
            )
        },
        containerColor = FixTheme.colors.surfaceAlt
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onMapQueryChange,
                placeholder = { Text(stringResource(R.string.map_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FixTheme.colors.textSecondary) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onMapQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_clear_search))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = FixTheme.colors.border,
                    focusedBorderColor = FixTheme.colors.primary,
                    unfocusedContainerColor = FixTheme.colors.surface,
                    focusedContainerColor = FixTheme.colors.surface
                )
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        label = stringResource(R.string.filter_all),
                        selected = selectedCategoryId == null,
                        onClick = { viewModel.onMapCategorySelected(null) }
                    )
                }
                // Real categories, the same list the customer picks from, instead of the invented
                // "🔥 Trending / ⭐ Recommended / 🛠 Electrician" chips that matched nothing.
                items(dummyCategories) { category ->
                    CategoryChip(
                        label = stringResource(category.titleRes),
                        selected = selectedCategoryId == category.id,
                        onClick = { viewModel.onMapCategorySelected(category.id) }
                    )
                }
            }

            MapPreviewNotice()

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = if (query.isNotBlank() || selectedCategoryId != null) {
                            stringResource(R.string.map_empty_filtered_title)
                        } else {
                            stringResource(R.string.map_empty_title)
                        },
                        description = if (query.isNotBlank() || selectedCategoryId != null) {
                            stringResource(R.string.map_empty_filtered_body)
                        } else {
                            stringResource(R.string.map_empty_body)
                        },
                        actionText = if (query.isNotBlank() || selectedCategoryId != null) stringResource(R.string.action_clear_filters) else null,
                        onAction = if (query.isNotBlank() || selectedCategoryId != null) {
                            {
                                viewModel.onMapQueryChange("")
                                viewModel.onMapCategorySelected(null)
                            }
                        } else null
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        WorkerJobCard(
                            task = task,
                            posterName = posterText(task.ownerId, ownerNames),
                            onAccept = { viewModel.acceptTask(task) },
                            onDecline = { viewModel.rejectTask(task) },
                            onMessage = { onOpenChat(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = FixTheme.colors.primary,
            selectedLabelColor = FixTheme.colors.onPrimary,
            containerColor = FixTheme.colors.surface,
            labelColor = FixTheme.colors.textPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) FixTheme.colors.primary else FixTheme.colors.border
        )
    )
}

/** Says outright that the map is missing, rather than drawing something that looks like one. */
@Composable
private fun MapPreviewNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(FixTheme.colors.infoSurface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Map,
            contentDescription = null,
            tint = FixTheme.colors.info,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.map_preview_notice),
            fontSize = 13.sp,
            color = FixTheme.colors.textPrimary,
            textAlign = TextAlign.Start
        )
    }
}
