package com.example.ui.screens.helper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.Spacing
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BlueContainer
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.SecondaryGrey

sealed class HelperScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : HelperScreen("helper_home", "Home", Icons.Default.Home)
    object Map : HelperScreen("helper_map", "Map", Icons.Default.Map)
    object Chat : HelperScreen("helper_chat", "Chats", Icons.AutoMirrored.Filled.Chat)
    object Tasks : HelperScreen("helper_tasks", "Tasks", Icons.AutoMirrored.Filled.Assignment)
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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val openChatTaskId by viewModel.openChatTaskId.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val activeChats = allTasks.count { it.status == "accepted" }

    val navigateToTab: (HelperScreen) -> Unit = { screen ->
        navController.navigate(screen.route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    val openChat: (Int) -> Unit = { taskId ->
        viewModel.openChat(taskId)
        navigateToTab(HelperScreen.Chat)
    }
    val isInConversation = openChatTaskId != null &&
        navBackStackEntry?.destination?.route == HelperScreen.Chat.route

    Scaffold(
        // Each tab draws its own top app bar, which already handles the status bar.
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.White,
        bottomBar = {
            if (!isInConversation) {
                HelperBottomBar(
                    currentRoute = { screen -> navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true },
                    chatBadgeCount = activeChats,
                    onSelect = navigateToTab
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = HelperScreen.Home.route,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            composable(HelperScreen.Home.route) {
                WorkerHomeScreen(
                    viewModel = viewModel,
                    onSwitchRole = onBackToRoles,
                    onOpenChat = openChat,
                    onSeeAllTasks = { navigateToTab(HelperScreen.Tasks) }
                )
            }
            composable(HelperScreen.Map.route) {
                WorkerMapScreen(viewModel = viewModel, onOpenChat = openChat)
            }
            composable(HelperScreen.Chat.route) {
                WorkerChatScreen(viewModel = viewModel, onExploreJobs = { navigateToTab(HelperScreen.Home) })
            }
            composable(HelperScreen.Tasks.route) {
                WorkerTasksScreen(
                    viewModel = viewModel,
                    onOpenChat = openChat,
                    onExploreJobs = { navigateToTab(HelperScreen.Home) }
                )
            }
        }
    }
}

/** Floating pill navigation that sits above the gesture / navigation bar. */
@Composable
private fun HelperBottomBar(
    currentRoute: (HelperScreen) -> Boolean,
    chatBadgeCount: Int,
    onSelect: (HelperScreen) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            helperBottomNavItems.forEach { screen ->
                val isSelected = currentRoute(screen)
                Surface(
                    onClick = { onSelect(screen) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = if (isSelected) BlueContainer else Color.Transparent
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tint = if (isSelected) BluePrimary else SecondaryGrey
                        if (screen == HelperScreen.Chat && chatBadgeCount > 0) {
                            BadgedBox(badge = { Badge(containerColor = BluePrimary) { Text(chatBadgeCount.toString()) } }) {
                                Icon(screen.icon, contentDescription = screen.title, tint = tint, modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Icon(screen.icon, contentDescription = screen.title, tint = tint, modifier = Modifier.size(24.dp))
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = screen.title,
                                color = BluePrimary,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Shared top-bar style for helper tabs: white, title on the left, no elevation tint. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperTopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            scrolledContainerColor = Color.White,
            titleContentColor = DarkNavy,
            actionIconContentColor = DarkNavy,
            navigationIconContentColor = DarkNavy
        )
    )
}
