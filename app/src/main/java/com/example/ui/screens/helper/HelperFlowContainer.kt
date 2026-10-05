package com.example.ui.screens.helper

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.screens.taskflow.categoryTitles
import com.example.ui.theme.FixTheme

sealed class HelperScreen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : HelperScreen("helper_home", R.string.nav_home, Icons.Default.Home)
    object Map : HelperScreen("helper_map", R.string.nav_map, Icons.Default.Map)
    object Chat : HelperScreen("helper_chat", R.string.nav_chat, Icons.AutoMirrored.Filled.Chat)
    object Tasks : HelperScreen("helper_tasks", R.string.nav_tasks, Icons.AutoMirrored.Filled.Assignment)
}

val helperBottomNavItems = listOf(
    HelperScreen.Home,
    HelperScreen.Map,
    HelperScreen.Chat,
    HelperScreen.Tasks
)

@Composable
fun HelperFlowContainer(
    onBackToRoles: () -> Unit,
    viewModel: HelperViewModel
) {
    // Search matches the category names the user can actually see, so the ViewModel is told what
    // they say in the active language. Recomposes when the language changes, because
    // stringResource does.
    val titles = categoryTitles()
    LaunchedEffect(titles) { viewModel.onCategoryTitlesChanged(titles) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = FixTheme.colors.background,
        // The same bar as the customer side. This used to be a floating pill that ignored the
        // system navigation bar, sat on a band of a different colour from the screen above it,
        // and hid every label except the selected one.
        bottomBar = {
            NavigationBar(containerColor = FixTheme.colors.surface) {
                helperBottomNavItems.forEach { screen ->
                    val isSelected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true
                    // NavigationBarItem is a selectable tab, so the active tab is announced as
                    // selected — the reason this was hand-built with selectable() before.
                    // The Chat tab carried a hardcoded "3" badge. Nothing records whether a
                    // message has been read, so there is no unread count to show.
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.titleRes), maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = FixTheme.colors.primary,
                            selectedTextColor = FixTheme.colors.primary,
                            unselectedIconColor = FixTheme.colors.textSecondary,
                            unselectedTextColor = FixTheme.colors.textSecondary,
                            indicatorColor = FixTheme.colors.primarySurface
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = HelperScreen.Home.route,
            // consumeWindowInsets: each tab draws its own top app bar, which would otherwise add
            // the status bar a second time on top of this Scaffold's padding.
            modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues)
        ) {
            // Asking a question about a job jumps to the Chat tab with that conversation open,
            // which is what makes the "Chat" action on a job card lead somewhere.
            fun openChat(taskId: Int) {
                viewModel.requestChat(taskId)
                navController.navigate(HelperScreen.Chat.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            composable(HelperScreen.Home.route) {
                WorkerHomeScreen(viewModel = viewModel, onOpenChat = ::openChat)
            }
            composable(HelperScreen.Map.route) {
                WorkerMapScreen(viewModel = viewModel, onOpenChat = ::openChat)
            }
            composable(HelperScreen.Chat.route) {
                WorkerChatScreen(viewModel = viewModel)
            }
            composable(HelperScreen.Tasks.route) {
                WorkerTasksScreen(viewModel = viewModel, onOpenChat = ::openChat)
            }
        }
    }
}
