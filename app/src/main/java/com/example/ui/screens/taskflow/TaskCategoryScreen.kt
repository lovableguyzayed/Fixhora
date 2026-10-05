package com.example.ui.screens.taskflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BottomActionBar
import com.example.ui.components.EmptyState
import com.example.ui.components.ErrorText
import com.example.ui.components.FixhoraCard
import com.example.ui.components.FixhoraTextField
import com.example.ui.components.IconBadge
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SectionTitle
import com.example.ui.components.Spacing
import com.example.ui.components.StepHeader
import com.example.ui.theme.*

@Composable
fun TaskCategoryScreen(onNext: () -> Unit, viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory = uiState.categoryId
    val query = uiState.searchCategoryQuery
    var showError by remember { mutableStateOf(false) }

    val filteredCategories = remember(query) {
        dummyCategories.filter {
            it.title.contains(query, ignoreCase = true) || it.subtitle.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StepHeader(
                    title = "What do you need help with?",
                    subtitle = "Choose the category that best fits your task"
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FixhoraTextField(
                    value = query,
                    onValueChange = { viewModel.updateSearchCategoryQuery(it) },
                    placeholder = "Search categories",
                    leadingIcon = Icons.Default.Search,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    trailingIcon = if (query.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchCategoryQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(20.dp))
                            }
                        }
                    } else null
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionTitle(
                    text = if (query.isBlank()) "Popular categories" else "Results",
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }

            if (filteredCategories.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "No matching category",
                        message = "Try a different word, or choose \"Other\" to describe any task.",
                        actionLabel = "Show all categories",
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
                ErrorText("Please select a category to continue", modifier = Modifier.padding(bottom = Spacing.xs))
            }
            PrimaryButton(
                text = "Continue",
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = {
                    if (selectedCategory != null) onNext() else showError = true
                }
            )
        }
    }
}

@Composable
private fun CategoryTile(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    FixhoraCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp),
        containerColor = if (isSelected) BlueContainer else androidx.compose.ui.graphics.Color.White,
        borderColor = if (isSelected) BluePrimary else BorderGrey
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = BluePrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(18.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconBadge(
                    icon = category.icon,
                    tint = category.iconTint,
                    size = 44.dp,
                    iconSize = 24.dp,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = category.title,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) BluePrimary else DarkNavy,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = category.subtitle,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = SecondaryGrey,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
