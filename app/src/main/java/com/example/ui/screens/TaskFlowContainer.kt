package com.example.ui.screens

import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
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
import com.example.R
import com.example.ui.screens.taskflow.*
import androidx.compose.foundation.border
import com.example.ui.components.CircleBackButton
import com.example.ui.components.FixButton
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Spacing

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.taskflow.TaskViewModel
import com.example.FixhoraApplication

sealed class TaskScreen(val route: String, val index: Int, @StringRes val titleRes: Int) {
    object Category : TaskScreen("category", 1, R.string.step_category)
    object Location : TaskScreen("location", 2, R.string.step_location)
    // Route kept as "photos" for stability; the step holds the title, details and budget as well,
    // so it is labelled "Details" rather than "Photos".
    object Photos : TaskScreen("photos", 3, R.string.step_details)
    object Review : TaskScreen("review", 4, R.string.step_review)
}

val taskScreens = listOf(TaskScreen.Category, TaskScreen.Location, TaskScreen.Photos, TaskScreen.Review)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFlowContainer(onBackToRoles: () -> Unit, onTaskPosted: () -> Unit = {}) {
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
            containerColor = FixTheme.colors.surface,
            title = { Text(stringResource(R.string.discard_title), color = FixTheme.colors.textPrimary) },
            text = {
                Text(stringResource(R.string.discard_body), color = FixTheme.colors.textSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    viewModel.discardDraft()
                    onBackToRoles()
                }) {
                    Text(stringResource(R.string.action_discard), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.action_keep_editing), color = FixTheme.colors.primary)
                }
            }
        )
    }

    Scaffold(
        containerColor = FixTheme.colors.background,
        topBar = {
            if (currentRoute != "success") {
                // Centre-aligned so the title sits in the middle of the screen whatever the
                // navigation icon and actions are; a fillMaxWidth title in a TopAppBar is centred
                // only in the space left over.
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.flow_i_need_help),
                            style = MaterialTheme.typography.titleMedium,
                            color = FixTheme.colors.textPrimary
                        )
                    },
                    navigationIcon = {
                        CircleBackButton(
                            onClick = { goBack() },
                            contentDescription = stringResource(R.string.cd_back),
                            modifier = Modifier.padding(start = Spacing.xs)
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = FixTheme.colors.background,
                        scrolledContainerColor = FixTheme.colors.background
                    )
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
                .background(FixTheme.colors.background)
        ) {
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
                            // Back to that step with the steps before it intact, rather than
                            // rebuilding it; Continue walks forward again from there.
                            onNavigateToStep = { route ->
                                navController.popBackStack(route, inclusive = false)
                            },
                            viewModel = viewModel
                        )
                    }
                    composable("success") {
                        TaskPostedScreen(onViewTasks = onTaskPosted, onBackHome = onBackToRoles)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskPostedScreen(onViewTasks: () -> Unit, onBackHome: () -> Unit) {
    val colors = FixTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(112.dp).clip(CircleShape).background(colors.successSurface),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(colors.success),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(modifier = Modifier.height(Spacing.xl))
        Text(
            stringResource(R.string.success_title),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        // The old copy promised "you will be notified once someone accepts your task". There are
        // no notifications in this app, so that was a promise it could not keep. Checking back is
        // what actually works.
        Text(
            stringResource(R.string.success_body),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(Spacing.xxxl))
        FixButton(text = stringResource(R.string.success_view_tasks), onClick = onViewTasks)
        Spacer(modifier = Modifier.height(Spacing.sm))
        TextButton(onClick = onBackHome, modifier = Modifier.fillMaxWidth().heightIn(min = MinTouchTarget)) {
            Text(stringResource(R.string.success_back_home), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
        }
    }
}

/**
 * Four evenly spaced steps. Each step owns an equal slice of the width with its circle centred in
 * it, so every label sits directly under its own circle and the connector runs through the
 * circles. The previous layout spread the circles edge to edge but gave the labels equal columns,
 * so only the middle of the row lined up.
 */
@Composable
fun StepProgressBar(currentStep: Int) {
    val colors = FixTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.md)
    ) {
        taskScreens.forEachIndexed { index, screen ->
            val step = index + 1
            val isDone = step < currentStep
            val isCurrent = step == currentStep
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepConnector(visible = index > 0, active = step <= currentStep, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) colors.primarySurface else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isDone || isCurrent) colors.primary else colors.surface)
                                .border(1.dp, if (isDone || isCurrent) colors.primary else colors.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
                            } else {
                                Text(
                                    text = step.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) colors.onPrimary else colors.textSecondary
                                )
                            }
                        }
                    }
                    StepConnector(visible = index < taskScreens.lastIndex, active = step < currentStep, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(screen.titleRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Medium,
                    color = when {
                        isCurrent -> colors.primary
                        isDone -> colors.textPrimary
                        else -> colors.textSecondary
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
                    active -> FixTheme.colors.primary
                    else -> FixTheme.colors.border
                }
            )
    )
}
