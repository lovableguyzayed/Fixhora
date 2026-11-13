package com.example.ui.screens.shell

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.SecondaryGrey

data class ShellTab(val route: String, val label: Int, val icon: ImageVector)

/**
 * Bottom tabs (icon + label, Material 3). Tabs keep their own state and scroll position.
 * On the first tab, Back asks "Press back again to exit" instead of closing at once.
 */
@Composable
fun ShellScaffold(
    tabs: List<ShellTab>,
    navController: NavHostController = rememberNavController(),
    content: NavGraphBuilder.(NavHostController) -> Unit,
) {
    val entry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val exitMessage = stringResource(R.string.press_back_again)
    var lastBack by remember { mutableLongStateOf(0L) }
    val onFirstTab = entry?.destination?.route == tabs.first().route

    BackHandler(enabled = onFirstTab) {
        val now = System.currentTimeMillis()
        if (now - lastBack < 2000) (context as? android.app.Activity)?.finish()
        else { lastBack = now; Toast.makeText(context, exitMessage, Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEach { tab ->
                    val selected = entry?.destination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.label), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary, selectedTextColor = BluePrimary,
                            indicatorColor = BluePrimary.copy(alpha = 0.12f),
                            unselectedIconColor = SecondaryGrey, unselectedTextColor = SecondaryGrey,
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = tabs.first().route, modifier = Modifier.padding(padding)) {
            content(navController)
        }
    }
}

/** Opens another tab of the same shell (e.g. "See jobs near you" from an empty list). */
fun NavHostController.openTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
