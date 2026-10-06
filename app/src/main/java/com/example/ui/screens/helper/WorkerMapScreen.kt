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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.launch
import com.example.R
import com.example.ui.components.CircleBackButton
import com.example.ui.format.posterText
import com.example.ui.components.EmptyState
import com.example.ui.components.SearchField
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
fun WorkerMapScreen(viewModel: HelperViewModel, onOpenChat: (Int) -> Unit, onBack: () -> Unit = {}) {
    val tasks by viewModel.mapTasks.collectAsState()
    val query by viewModel.mapQuery.collectAsState()
    val selectedCategoryId by viewModel.mapCategoryId.collectAsState()
    val ownerNames by viewModel.ownerNames.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    CircleBackButton(
                        onClick = onBack,
                        contentDescription = stringResource(R.string.cd_back),
                        modifier = Modifier.padding(start = Spacing.xs)
                    )
                },
                title = { Text(stringResource(R.string.map_title), fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FixTheme.colors.surface)
            )
        },
        containerColor = FixTheme.colors.surfaceAlt
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            SearchField(
                query = query,
                onQueryChange = viewModel::onMapQueryChange,
                placeholder = stringResource(R.string.map_search_hint),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                val locations = rememberJobLocations(tasks)
                val listState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

                JobsMap(
                    tasks = tasks,
                    locations = locations,
                    isDark = isDark,
                    // A pin is only useful if it leads to the job, so tapping one brings its card
                    // into view in the list.
                    onJobSelected = { id ->
                        val index = tasks.indexOfFirst { it.id == id }
                        if (index >= 0) scope.launch { listState.animateScrollToItem(index) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        .height(260.dp)
                )
                Text(
                    text = when {
                        locations.resolving && locations.points.isEmpty() -> stringResource(R.string.map_locating)
                        locations.points.isEmpty() -> stringResource(R.string.map_none_placed)
                        else -> stringResource(R.string.map_placed_count, locations.points.size, tasks.size)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = FixTheme.colors.textSecondary,
                    modifier = Modifier.padding(horizontal = Spacing.lg)
                )

                LazyColumn(
                    state = listState,
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
