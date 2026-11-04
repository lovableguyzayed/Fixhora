package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.FixhoraApplication
import com.example.R
import com.example.ui.components.BentoCard
import com.example.ui.components.FixButton
import com.example.ui.components.FixButtonStyle
import com.example.ui.components.FixTopBar
import com.example.ui.screens.taskflow.*
import com.example.ui.screens.taskflow.TaskViewModel
import com.example.ui.theme.Bento
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Spacing

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
                FixTopBar(
                    title = stringResource(R.string.flow_i_need_help),
                    subtitle = stringResource(R.string.step_counter, currentScreen.index, taskScreens.size),
                    onBack = { goBack() }
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
    val bento = Bento.Lime
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .padding(horizontal = Spacing.xl),
        verticalArrangement = Arrangement.Center
    ) {
        BentoCard(colors = bento, modifier = Modifier.fillMaxWidth(), contentPadding = Spacing.xl) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(colors.ink),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = colors.onInk, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
            Text(
                stringResource(R.string.success_title),
                style = MaterialTheme.typography.displaySmall,
                color = bento.onFill
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            // The old copy promised "you will be notified once someone accepts your task". There
            // are no notifications in this app, so that was a promise it could not keep. Checking
            // back is what actually works.
            Text(
                stringResource(R.string.success_body),
                style = MaterialTheme.typography.bodyLarge,
                color = bento.onFillSecondary
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
            FixButton(
                text = stringResource(R.string.success_view_tasks),
                onClick = onViewTasks,
                style = FixButtonStyle.INK
            )
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        TextButton(onClick = onBackHome, modifier = Modifier.fillMaxWidth().heightIn(min = MinTouchTarget)) {
            Text(stringResource(R.string.success_back_home), style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
        }
    }
}

/**
 * The four steps as a row of pills: done ones are black with a check, the current one is the
 * yellow highlight, the rest are outlined. Equal widths, so the row never scrolls or wraps.
 *
 * The whole row is announced as one "Step 2 of 4: Location" rather than as four loose words.
 */
@Composable
fun StepProgressBar(currentStep: Int) {
    val colors = FixTheme.colors
    val currentLabel = stringResource(taskScreens[(currentStep - 1).coerceIn(0, taskScreens.lastIndex)].titleRes)
    val summary = stringResource(R.string.step_counter, currentStep, taskScreens.size) + ": " + currentLabel
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
            .clearAndSetSemantics { contentDescription = summary },
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        taskScreens.forEachIndexed { index, screen ->
            val step = index + 1
            val isDone = step < currentStep
            val isCurrent = step == currentStep
            val container = when {
                isCurrent -> colors.highlight
                isDone -> colors.ink
                else -> colors.surface
            }
            val content = when {
                isCurrent -> colors.onHighlight
                isDone -> colors.onInk
                else -> colors.textSecondary
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(container)
                    .then(if (!isDone && !isCurrent) Modifier.border(1.dp, colors.border, CircleShape) else Modifier)
                    .padding(horizontal = Spacing.sm),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text(
                    text = stringResource(screen.titleRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
