package com.example.ui.screens.helper

import androidx.compose.ui.res.stringResource
import com.example.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.room.TaskEntity
import com.example.ui.screens.taskflow.dummyCategories
import com.example.ui.theme.*
import com.example.util.IndiaFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerMapScreen(viewModel: HelperViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val loadedTasks by viewModel.availableTasks.collectAsState()
    val isLoading = loadedTasks == null
    val tasks = loadedTasks.orEmpty()
    
    var selectedTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showBidSheet by remember { mutableStateOf(false) }


    val categories = listOf("All", "🔥 Trending", "⭐ Recommended", "🛠 Electrician", "🚰 Plumber", "🎨 Painter", "🧹 Cleaner", "🔧 Mechanic")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workermap_interactive_map_view), fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { /* Open Filters */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.workermap_filters), tint = DarkNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 80.dp) // padding to avoid bottom navigation overlapping
            ) {
                FloatingActionButton(
                    onClick = { /* Refresh */ },
                    containerColor = Color.White,
                    contentColor = DarkNavy,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.workermap_refresh_nearby_jobs))
                }
                FloatingActionButton(
                    onClick = { /* Current Location */ },
                    containerColor = BluePrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.workermap_current_location))
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Mock Map Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE0E0E0)) // Light grey map placeholder
            ) {
                // Placeholder map grid lines or text
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(100.dp), tint = SecondaryGrey.copy(alpha = 0.3f))
                    Text(stringResource(R.string.workermap_interactive_map_view), color = SecondaryGrey, fontWeight = FontWeight.Bold)
                }

                // Mock Markers
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                } else if (tasks.isNotEmpty()) {
                    // Dummy positions for markers
                    val positions = listOf(
                        Alignment.TopStart, Alignment.TopEnd, Alignment.CenterStart, Alignment.CenterEnd, Alignment.BottomStart
                    )
                    
                    tasks.take(5).forEachIndexed { index, task ->
                        Box(
                            modifier = Modifier
                                .align(positions[index % positions.size])
                                .padding(top = 100.dp, bottom = 100.dp, start = 40.dp, end = 40.dp)
                                .offset(
                                    x = (index * 20).dp,
                                    y = (index * 30).dp
                                )
                        ) {
                            MapMarker(
                                task = task,
                                onClick = { selectedTask = task }
                            )
                        }
                    }
                }
            }

            // Top UI Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.workermap_search_jobs)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.workermap_voice_search)) },
                        modifier = Modifier
                            .weight(1f)
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .background(Color.White, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == category,
                                borderColor = if (selectedCategory == category) BluePrimary else BorderGrey
                            )
                        )
                    }
                }
            }
        }

        // Job Details Bottom Sheet
        if (selectedTask != null && !showBidSheet) {
            ModalBottomSheet(
                onDismissRequest = { selectedTask = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
            ) {
                JobDetailsContent(
                    task = selectedTask!!,
                    onPlaceBidClick = { showBidSheet = true },
                    onAcceptClick = { 
                        viewModel.acceptTask(selectedTask!!)
                        selectedTask = null 
                    },
                    onChatClick = { /* Chat */ }
                )
            }
        }

        // Place Bid Bottom Sheet
        if (showBidSheet && selectedTask != null) {
            ModalBottomSheet(
                onDismissRequest = { showBidSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                PlaceBidContent(
                    task = selectedTask!!,
                    onSubmit = { 
                        viewModel.acceptTask(selectedTask!!) // Treating a placed bid as accepting for now for demo purposes
                        showBidSheet = false
                        selectedTask = null 
                    },
                    onCancel = { showBidSheet = false }
                )
            }
        }
    }
}

@Composable
fun MapMarker(task: TaskEntity, onClick: () -> Unit) {
    val isUrgent = true // Mock logic
    val markerColor = if (isUrgent) OrangeSecondary else BluePrimary
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .background(markerColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = IndiaFormat.rupeesOrNull(task.maxBudget) ?: "Bid",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = markerColor,
            modifier = Modifier.size(32.dp).offset(y = (-4).dp)
        )
    }
}

@Composable
fun JobDetailsContent(
    task: TaskEntity,
    onPlaceBidClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val category = dummyCategories.find { it.id == task.categoryId } ?: dummyCategories.first()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(BorderGrey),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SecondaryGrey)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.workerhome_customer_name), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = stringResource(R.string.workerhome_verified), tint = BluePrimary, modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("4.8", fontSize = 14.sp, color = SecondaryGrey)
                    }
                }
            }
            Surface(
                color = OrangeSecondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.workerhome_urgent), color = OrangeSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = task.descriptionTitle.ifEmpty { "Need Help with ${category.title}" },
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = task.descriptionDetails.ifEmpty { "Looking for someone to help me out with this task as soon as possible." },
            color = SecondaryGrey,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            JobDetailItem(icon = Icons.Default.LocationOn, title = stringResource(R.string.workermap_distance), value = "2.5 km away")
            JobDetailItem(icon = Icons.Default.AccessTime, title = stringResource(R.string.workermap_posted), value = "10 mins ago")
            JobDetailItem(
                icon = Icons.Default.AccountBalanceWallet,
                title = stringResource(R.string.workermap_budget),
                value = IndiaFormat.rupeesOrNull(task.maxBudget) ?: "Negotiable",
                valueColor = DarkNavy,
                valueWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onAcceptClick,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(stringResource(R.string.workerhome_accept_job), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onPlaceBidClick,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text(stringResource(R.string.workerhome_place_bid), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = onChatClick) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.workermap_chat_with_customer), fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun JobDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    valueColor: Color = SecondaryGrey,
    valueWeight: FontWeight = FontWeight.Medium
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).background(Color(0xFFF5F5F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 12.sp, color = SecondaryGrey)
            Text(value, fontSize = 14.sp, color = valueColor, fontWeight = valueWeight)
        }
    }
}

@Composable
fun PlaceBidContent(task: TaskEntity, onSubmit: () -> Unit, onCancel: () -> Unit) {
    var bidAmount by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(stringResource(R.string.workerhome_place_bid), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.workermap_submit_a_competitive_bid_for_this), fontSize = 16.sp, color = SecondaryGrey)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = bidAmount,
            onValueChange = { bidAmount = IndiaFormat.cleanAmount(it) },
            singleLine = true,
            label = { Text(stringResource(R.string.workermap_bid_amount)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = estimatedTime,
            onValueChange = { estimatedTime = it },
            label = { Text(stringResource(R.string.workermap_estimated_completion_time)) },
            placeholder = { Text(stringResource(R.string.workermap_e_g_2_hours_today_at)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(stringResource(R.string.workermap_message_to_customer_optional)) },
            placeholder = { Text(stringResource(R.string.workermap_i_have_5_years_of_experience)) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.workermap_cancel), fontSize = 16.sp)
            }
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(stringResource(R.string.workermap_submit_bid), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
