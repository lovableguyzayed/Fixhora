package com.example.ui.screens.helper

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerMapScreen(viewModel: HelperViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val tasks by viewModel.availableTasks.collectAsState()
    
    var selectedTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showBidSheet by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500)
        isLoading = false
    }

    val categories = listOf("All", "🔥 Trending", "⭐ Recommended", "🛠 Electrician", "🚰 Plumber", "🎨 Painter", "🧹 Cleaner", "🔧 Mechanic")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interactive Map View", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { /* Open Filters */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = DarkNavy)
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
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Nearby Jobs")
                }
                FloatingActionButton(
                    onClick = { /* Current Location */ },
                    containerColor = BluePrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Current Location")
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
                    Text("Interactive Map View", color = SecondaryGrey, fontWeight = FontWeight.Bold)
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
                        placeholder = { Text("Search jobs...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.Mic, contentDescription = "Voice Search") },
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
                text = if (task.maxBudget.isNotEmpty()) "₹${task.maxBudget}" else "Bid",
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
                        Text("Customer Name", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = BluePrimary, modifier = Modifier.size(16.dp))
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
                Text("Urgent", color = OrangeSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
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
            JobDetailItem(icon = Icons.Default.LocationOn, title = "Distance", value = "2.5 km away")
            JobDetailItem(icon = Icons.Default.AccessTime, title = "Posted", value = "10 mins ago")
            JobDetailItem(
                icon = Icons.Default.AccountBalanceWallet,
                title = "Budget",
                value = if (task.maxBudget.isNotEmpty()) "₹${task.maxBudget}" else "Negotiable",
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
                Text("Accept Job", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onPlaceBidClick,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text("Place Bid", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = onChatClick) {
                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat with Customer", fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
        Text("Place Bid", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Submit a competitive bid for this task.", fontSize = 16.sp, color = SecondaryGrey)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = bidAmount,
            onValueChange = { bidAmount = it },
            label = { Text("Bid Amount (₹)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = estimatedTime,
            onValueChange = { estimatedTime = it },
            label = { Text("Estimated Completion Time") },
            placeholder = { Text("e.g., 2 hours, Today at 5 PM") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message to Customer (Optional)") },
            placeholder = { Text("I have 5 years of experience...") },
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
                Text("Cancel", fontSize = 16.sp)
            }
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Submit Bid", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
