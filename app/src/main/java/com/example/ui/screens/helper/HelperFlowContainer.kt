package com.example.ui.screens.helper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.FixTheme

sealed class HelperScreen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : HelperScreen("helper_home", "Home", Icons.Default.Home)
    object Map : HelperScreen("helper_map", "Map", Icons.Default.Map)
    object Chat : HelperScreen("helper_chat", "Chat", Icons.AutoMirrored.Filled.Chat)
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
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    helperBottomNavItems.forEach { screen ->
                        val isSelected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                .background(if (isSelected) FixTheme.colors.primary.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (screen == HelperScreen.Chat) {
                                    BadgedBox(badge = { Badge { Text("3") } }) {
                                        Icon(
                                            screen.icon,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) FixTheme.colors.primary else com.example.ui.theme.FixTheme.colors.textSecondary,
                                            modifier = Modifier.size(if (isSelected) 24.dp else 22.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        screen.icon,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) FixTheme.colors.primary else com.example.ui.theme.FixTheme.colors.textSecondary,
                                        modifier = Modifier.size(if (isSelected) 24.dp else 22.dp)
                                    )
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = screen.title,
                                        color = FixTheme.colors.primary,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = HelperScreen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(HelperScreen.Home.route) {
                WorkerHomeScreen(viewModel = viewModel)
            }
            composable(HelperScreen.Map.route) {
                WorkerMapScreen(viewModel = viewModel)
            }
            composable(HelperScreen.Chat.route) {
                WorkerChatScreen(viewModel = viewModel)
            }
            composable(HelperScreen.Tasks.route) {
                WorkerTasksScreen(viewModel = viewModel)
            }
        }
    }
}
