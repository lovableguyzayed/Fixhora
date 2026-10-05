package com.example.ui.screens.taskflow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BottomActionBar
import com.example.ui.components.EmptyState
import com.example.ui.components.FixButton
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

@Composable
fun TaskCategoryScreen(onNext: () -> Unit, viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory = uiState.categoryId
    val query = uiState.searchCategoryQuery
    var showError by remember { mutableStateOf(false) }

    val filteredCategories = dummyCategories.filter {
        stringResource(it.titleRes).contains(query, ignoreCase = true) ||
            stringResource(it.subtitleRes).contains(query, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header, search and tiles scroll as one; the grid used to scroll inside a fixed header,
        // leaving it a sliver of the screen on small phones.
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = Spacing.xl, end = Spacing.xl, bottom = Spacing.xl),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StepHeader(
                    title = stringResource(R.string.task_category_title),
                    subtitle = stringResource(R.string.task_category_subtitle)
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                StepTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchCategoryQuery(it) },
                    placeholder = stringResource(R.string.task_category_search),
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchCategoryQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_clear_search))
                            }
                        }
                    } else null
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.task_category_popular),
                    style = MaterialTheme.typography.titleLarge,
                    color = FixTheme.colors.textPrimary,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }

            if (filteredCategories.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = stringResource(R.string.task_category_empty_title),
                        description = stringResource(R.string.task_category_empty_body),
                        actionText = stringResource(R.string.action_clear_search),
                        onAction = { viewModel.updateSearchCategoryQuery("") }
                    )
                }
            } else {
                items(filteredCategories, key = { it.id }) { category ->
                    CategoryTile(
                        category = category,
                        isSelected = selectedCategory == category.id,
                        onClick = {
                            viewModel.updateCategory(category.id)
                            showError = false
                        }
                    )
                }
            }
        }

        BottomActionBar {
            if (showError) {
                Text(
                    text = stringResource(R.string.task_category_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
            }
            FixButton(
                text = stringResource(R.string.action_continue),
                onClick = {
                    if (selectedCategory != null) onNext() else showError = true
                }
            )
        }
    }
}

/**
 * One category. A fixed height with room for two lines of title and two of description, so long
 * labels (and Hindi, which runs longer) wrap inside the tile instead of being clipped by an aspect
 * ratio, and every tile in a row stays the same size.
 */
@Composable
private fun CategoryTile(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    val colors = FixTheme.colors
    val shape = RoundedCornerShape(Radius.lg)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(152.dp)
            .clip(shape)
            // selectable, not clickable: the border and tint show the choice visually, but only
            // this reports it to a screen reader.
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        shape = shape,
        color = if (isSelected) colors.primarySurface else colors.surface,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.primary else colors.border)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.cd_category_selected),
                    tint = colors.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(18.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(category.iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(category.icon, contentDescription = null, tint = category.iconTint, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = stringResource(category.titleRes),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) colors.primary else colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(category.subtitleRes),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
