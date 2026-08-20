package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.taskflow.*
import com.example.ui.theme.FixTheme

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.taskflow.TaskViewModel
import com.example.FixhoraApplication

sealed class TaskScreen(val route: String, val index: Int, val title: String) {
    object Category : TaskScreen("category", 1, "Task Details")
    object Location : TaskScreen("location", 2, "Location")
    object Photos : TaskScreen("photos", 3, "Photos")
    object Review : TaskScreen("review", 4, "Review")
}

val taskScreens = listOf(TaskScreen.Category, TaskScreen.Location, TaskScreen.Photos, TaskScreen.Review)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFlowContainer(onBackToRoles: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = taskScreens.find { it.route == currentRoute } ?: TaskScreen.Category
    
    val application = LocalContext.current.applicationContext as FixhoraApplication
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModel.Factory(
            application.taskRepository,
            application.sessionManager,
            application.taskPhotoStore,
            application.locationProvider
        )
    )

    var showDiscardDialog by remember { mutableStateOf(false) }

    // Leaving the flow with work in progress is the one place a user can silently lose everything
    // they typed, so it asks first. With an empty form there is nothing to warn about.
    fun attemptLeaveFlow() {
        if (currentRoute == "success") {
            onBackToRoles()
        } else if (viewModel.hasUnsavedContent()) {
            showDiscardDialog = true
        } else {
            onBackToRoles()
        }
    }

    fun goBack() {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            attemptLeaveFlow()
        }
    }

    BackHandler(enabled = currentRoute != "success") { goBack() }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard this task?") },
            text = {
                Text("Your category, details and photos for this task will be removed. This cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.discardDraft()
                    onBackToRoles()
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep editing", color = FixTheme.colors.primary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (currentRoute != "success") {
                TopAppBar(
                    title = { 
                        Text(
                            "I need help", 
                            modifier = Modifier.fillMaxWidth(), 
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { goBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = "Back",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp)
                            )
                        }
                    },
                    actions = {
                        // The "Skip" action that used to sit on the Photos step is gone: that
                        // screen also holds the task title, which is required, so skipping it
                        // walked straight past the validation and posted an untitled task.
                        if (currentScreen == TaskScreen.Review) {
                            TextButton(onClick = {
                                // Return to the first step, keeping the steps in between so the
                                // user can walk forward again. The previous version popped the
                                // whole flow inclusively and rebuilt it from scratch.
                                navController.popBackStack(TaskScreen.Category.route, inclusive = false)
                            }) {
                                Text(
                                    text = "Edit",
                                    color = FixTheme.colors.primary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Progress Bar indicator
            if (currentRoute != "success") {
                StepProgressBar(currentStep = currentScreen.index)
            }
            
            Box(modifier = Modifier.weight(1f)) {
                NavHost(navController = navController, startDestination = TaskScreen.Category.route) {
                    composable(TaskScreen.Category.route) {
                        TaskCategoryScreen(onNext = { navController.navigate(TaskScreen.Location.route) }, viewModel)
                    }
                    composable(TaskScreen.Location.route) {
                        TaskLocationScreen(onNext = { navController.navigate(TaskScreen.Photos.route) }, viewModel)
                    }
                    composable(TaskScreen.Photos.route) {
                        TaskPhotosScreen(onNext = { navController.navigate(TaskScreen.Review.route) }, viewModel)
                    }
                    composable(TaskScreen.Review.route) {
                        TaskReviewScreen(
                            onSubmit = { 
                                navController.navigate("success") { popUpTo(TaskScreen.Category.route) { inclusive = true } }
                            },
                            onNavigateToStep = { route ->
                                navController.navigate(route) {
                                    popUpTo(route) { inclusive = true }
                                }
                            },
                            viewModel = viewModel
                        )
                    }
                    composable("success") {
                        Column(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.size(80.dp).clip(CircleShape).background(FixTheme.colors.successSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = FixTheme.colors.success, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Task Posted successfully!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("We are finding helpers nearby. You will be notified once someone accepts your task.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(48.dp))
                            Button(
                                onClick = onBackToRoles,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FixTheme.colors.primary)
                            ) {
                                Text("Return to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepProgressBar(currentStep: Int) {
    val totalSteps = 4
    
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..totalSteps) {
                // Step Circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (i <= currentStep) FixTheme.colors.primary else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (i < currentStep) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = FixTheme.colors.onPrimary, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            text = i.toString(),
                            color = if (i == currentStep) FixTheme.colors.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Line
                if (i < totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (i < currentStep) FixTheme.colors.primary else MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            taskScreens.forEachIndexed { index, screen ->
                Text(
                    text = screen.title,
                    fontSize = 12.sp,
                    color = if (index + 1 <= currentStep) FixTheme.colors.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
