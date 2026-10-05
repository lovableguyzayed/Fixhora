package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.FixhoraApplication
import com.example.ui.components.BackButton
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.components.Spacing
import com.example.ui.screens.taskflow.*
import com.example.ui.theme.*

sealed class TaskScreen(val route: String, val index: Int, val title: String) {
    object Category : TaskScreen("category", 1, "Category")
    object Location : TaskScreen("location", 2, "Location")
    object Details : TaskScreen("details", 3, "Details")
    object Review : TaskScreen("review", 4, "Review")
}

val taskScreens = listOf(TaskScreen.Category, TaskScreen.Location, TaskScreen.Details, TaskScreen.Review)

private const val SuccessRoute = "success"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFlowContainer(onBackToRoles: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = taskScreens.find { it.route == currentRoute } ?: TaskScreen.Category
    val isSuccess = currentRoute == SuccessRoute

    val application = LocalContext.current.applicationContext as FixhoraApplication
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModel.Factory(application.taskRepository)
    )
    val draft by viewModel.uiState.collectAsState()
    var showLeaveDialog by remember { mutableStateOf(false) }

    // Leaving from the first step: confirm if the user has already started a task.
    val leaveFlow = {
        if (draft.categoryId != null || draft.descriptionTitle.isNotBlank()) showLeaveDialog = true else onBackToRoles()
    }
    val onBack: () -> Unit = {
        if (navController.previousBackStackEntry != null) navController.popBackStack() else leaveFlow()
    }
    BackHandler(enabled = !isSuccess && currentScreen == TaskScreen.Category) { leaveFlow() }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            if (!isSuccess) {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Post a Task", style = MaterialTheme.typography.titleMedium, color = DarkNavy)
                    },
                    navigationIcon = { BackButton(onClick = onBack, modifier = Modifier.padding(start = Spacing.xxs)) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            if (!isSuccess) {
                StepProgressBar(currentStep = currentScreen.index)
            }

            Box(modifier = Modifier.weight(1f)) {
                NavHost(navController = navController, startDestination = TaskScreen.Category.route) {
                    composable(TaskScreen.Category.route) {
                        TaskCategoryScreen(onNext = { navController.navigate(TaskScreen.Location.route) }, viewModel)
                    }
                    composable(TaskScreen.Location.route) {
                        TaskLocationScreen(onNext = { navController.navigate(TaskScreen.Details.route) }, viewModel)
                    }
                    composable(TaskScreen.Details.route) {
                        TaskPhotosScreen(onNext = { navController.navigate(TaskScreen.Review.route) }, viewModel)
                    }
                    composable(TaskScreen.Review.route) {
                        TaskReviewScreen(
                            onSubmit = {
                                navController.navigate(SuccessRoute) { popUpTo(TaskScreen.Category.route) { inclusive = true } }
                            },
                            // Jump back to an earlier step; the steps after it are re-entered with Continue.
                            onNavigateToStep = { route -> navController.popBackStack(route, inclusive = false) },
                            viewModel = viewModel
                        )
                    }
                    composable(SuccessRoute) {
                        TaskPostedScreen(
                            onDone = onBackToRoles,
                            onPostAnother = {
                                navController.navigate(TaskScreen.Category.route) {
                                    popUpTo(SuccessRoute) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = Color.White,
            title = { Text("Leave task posting?", style = MaterialTheme.typography.titleLarge, color = DarkNavy) },
            text = {
                Text(
                    "Your draft is saved. You can pick up where you left off next time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryGrey
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    onBackToRoles()
                }) { Text("Leave", color = BluePrimary, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Keep editing", color = SecondaryGrey) }
            }
        )
    }
}

/**
 * Four evenly spaced steps. Each step owns an equal slice of the width, so the circle and
 * its label are always centred on the same axis and the connector runs through the circles.
 */
@Composable
fun StepProgressBar(currentStep: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xs, vertical = Spacing.sm)
    ) {
        taskScreens.forEachIndexed { index, screen ->
            val step = index + 1
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepConnector(
                        visible = index > 0,
                        active = step <= currentStep,
                        modifier = Modifier.weight(1f)
                    )
                    StepCircle(step = step, currentStep = currentStep)
                    StepConnector(
                        visible = index < taskScreens.lastIndex,
                        active = step < currentStep,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = screen.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (step == currentStep) FontWeight.SemiBold else FontWeight.Medium,
                    color = when {
                        step == currentStep -> BluePrimary
                        step < currentStep -> DarkNavy
                        else -> HintGrey
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StepConnector(visible: Boolean, active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(2.dp)
            .background(
                when {
                    !visible -> Color.Transparent
                    active -> BluePrimary
                    else -> BorderGrey
                }
            )
    )
}

@Composable
private fun StepCircle(step: Int, currentStep: Int) {
    val isDone = step < currentStep
    val isCurrent = step == currentStep
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (isCurrent) BlueContainer else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (isDone || isCurrent) BluePrimary else Color.White)
                .border(1.dp, if (isDone || isCurrent) BluePrimary else BorderGrey, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) Color.White else HintGrey
                )
            }
        }
    }
}

@Composable
private fun TaskPostedScreen(onDone: () -> Unit, onPostAnother: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen, vertical = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Spacing.xxl))
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(SuccessContainer),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text("Task posted!", style = MaterialTheme.typography.headlineSmall, color = DarkNavy)
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            "We're notifying helpers near you. You'll be alerted as soon as someone sends an offer.",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryGrey,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.xl))
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MutedBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("What happens next", style = MaterialTheme.typography.titleSmall, color = DarkNavy)
                NextStepRow(1, "Nearby helpers review your task")
                NextStepRow(2, "Compare offers and chat with helpers")
                NextStepRow(3, "Pick the best helper and get it done")
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
        PrimaryButton(text = "Back to Home", onClick = onDone)
        Spacer(modifier = Modifier.height(Spacing.sm))
        SecondaryButton(text = "Post Another Task", onClick = onPostAnother)
    }
}

@Composable
private fun NextStepRow(number: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(BluePrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(number.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BluePrimary)
        }
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = DarkNavy)
    }
}
