package com.example.ui.screens.helper

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.room.TaskEntity
import com.example.ui.components.Spacing
import com.example.ui.components.StylizedMap
import com.example.ui.components.UserLocationDot
import com.example.ui.components.areaLabel
import com.example.ui.components.formatRupees
import com.example.ui.screens.taskflow.categoryFor
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*
import kotlinx.coroutines.delay

// Well-spread marker slots (fractions of the free map area) so pins never stack up.
private val markerSlots = listOf(
    0.08f to 0.04f, 0.62f to 0.02f, 0.30f to 0.24f, 0.78f to 0.30f,
    0.04f to 0.50f, 0.66f to 0.62f, 0.22f to 0.80f, 0.74f to 0.88f
)

@Composable
fun WorkerMapScreen(viewModel: HelperViewModel, onOpenChat: (Int) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    val tasks by viewModel.availableTasks.collectAsState()
    var sheet by remember { mutableStateOf<JobSheet?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        delay(700)
        isLoading = false
    }

    val visibleTasks = tasks.filter { task ->
        (selectedCategoryId == null || task.categoryId == selectedCategoryId) &&
            (searchQuery.isBlank() ||
                task.descriptionTitle.contains(searchQuery, ignoreCase = true) ||
                task.descriptionDetails.contains(searchQuery, ignoreCase = true) ||
                task.areaLabel().contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            HelperTopBar(
                title = {
                    Column {
                        Text("Jobs near you", style = MaterialTheme.typography.titleMedium, color = DarkNavy)
                        Text(
                            "Sector 62, Noida",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryGrey
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { refreshKey++ },
                containerColor = Color.White,
                contentColor = DarkNavy,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh nearby jobs")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            StylizedMap(modifier = Modifier.fillMaxSize())
            UserLocationDot(modifier = Modifier.align(Alignment.Center))

            // Markers live below the search/filter overlay and above the bottom pill.
            if (!isLoading) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 132.dp, bottom = 88.dp, start = Spacing.md, end = Spacing.md)
                ) {
                    val markerWidth = 96.dp
                    val markerHeight = 56.dp
                    visibleTasks.take(markerSlots.size).forEachIndexed { index, task ->
                        val (fx, fy) = markerSlots[index]
                        MapMarker(
                            task = task,
                            onClick = { sheet = JobSheet.Details(task) },
                            modifier = Modifier.offset(
                                x = (maxWidth - markerWidth) * fx,
                                y = (maxHeight - markerHeight) * fy
                            )
                        )
                    }
                }
            }

            // Search + category filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.sm)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search jobs or areas") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(20.dp))
                            }
                        }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md)
                        .shadow(6.dp, RoundedCornerShape(28.dp), ambientColor = DarkNavy.copy(alpha = 0.08f), spotColor = DarkNavy.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = BluePrimary,
                        focusedLeadingIconColor = BluePrimary,
                        unfocusedLeadingIconColor = SecondaryGrey,
                        focusedPlaceholderColor = HintGrey,
                        unfocusedPlaceholderColor = HintGrey,
                        focusedTextColor = DarkNavy,
                        unfocusedTextColor = DarkNavy
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    item {
                        MapFilterChip(
                            label = "All",
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null }
                        )
                    }
                    items(dummyCategories, key = { it.id }) { category ->
                        MapFilterChip(
                            label = category.title,
                            selected = selectedCategoryId == category.id,
                            onClick = {
                                selectedCategoryId = if (selectedCategoryId == category.id) null else category.id
                            },
                            leadingIcon = {
                                Icon(category.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            // Result count / loading pill
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Spacing.md)
                    .shadow(4.dp, RoundedCornerShape(50)),
                shape = RoundedCornerShape(50),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = BluePrimary, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Finding jobs…", style = MaterialTheme.typography.titleSmall, color = DarkNavy)
                    } else {
                        Icon(Icons.Default.WorkOutline, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = when (visibleTasks.size) {
                                0 -> "No jobs match your filters"
                                1 -> "1 job in this area"
                                else -> "${visibleTasks.size} jobs in this area"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = DarkNavy
                        )
                    }
                }
            }
        }
    }

    JobSheetHost(sheet = sheet, onSheetChange = { sheet = it }, viewModel = viewModel, onOpenChat = onOpenChat)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold) },
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            labelColor = DarkNavy,
            iconColor = SecondaryGrey,
            selectedContainerColor = BluePrimary,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = BorderGrey,
            selectedBorderColor = BluePrimary
        ),
        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp)
    )
}

/** Price pill with a pointer, coloured by the job's category. */
@Composable
private fun MapMarker(task: TaskEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val category = categoryFor(task.categoryId) ?: dummyCategories.last()
    val price = task.maxBudget.ifBlank { task.minBudget }
    // The whole marker is the touch target; a clickable Surface would pad the pill to 48dp
    // and detach the pointer from it.
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = category.iconTint,
            shadowElevation = 4.dp,
            border = BorderStroke(2.dp, Color.White)
        ) {
            Row(
                modifier = Modifier.padding(start = 6.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(category.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (price.isNotBlank()) formatRupees(price) else "Bid",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Canvas(
            modifier = Modifier
                .offset(y = (-1).dp)
                .size(width = 12.dp, height = 7.dp)
        ) {
            val pointer = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(pointer, category.iconTint)
        }
    }
}
