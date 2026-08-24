package com.example.ui.screens.mytasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.TaskFlowContainer
import com.example.ui.theme.FixTheme

/** The two things a customer does: ask for something, and keep track of what they asked for. */
private enum class CustomerTab(val label: String, val icon: ImageVector) {
  POST("Post a task", Icons.Default.PostAdd),
  MY_TASKS("My tasks", Icons.Default.Assignment),
}

/**
 * The customer's home.
 *
 * Until now "I need help" dropped straight into the four-step post wizard and there was nowhere
 * else to go: once a task was posted it was invisible to the person who posted it. The wizard is
 * unchanged and still the first tab; the second is everything they have asked for.
 */
@Composable
fun CustomerFlowContainer(
  viewModel: MyTasksViewModel,
  onBackToRoles: () -> Unit,
) {
  var tab by remember { mutableStateOf(CustomerTab.POST) }
  val tasks by viewModel.tasks.collectAsStateWithLifecycle()
  val openTask by viewModel.openTask.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()

  val task = openTask
  if (tab == CustomerTab.MY_TASKS && task != null) {
    // Without this, system back skips the open task and leaves the customer flow entirely, losing
    // the conversation they were reading.
    BackHandler(onBack = viewModel::closeTask)

    // Full screen, no bottom bar: the conversation needs the height, and the tab strip would
    // compete with the input row for the bottom of the screen.
    MyTaskDetailScreen(
      task = task,
      messages = messages,
      onBack = viewModel::closeTask,
      onSend = viewModel::sendMessage,
      onCancelTask = viewModel::cancelOpenTask,
    )
    return
  }

  Scaffold(
    containerColor = FixTheme.colors.background,
    bottomBar = {
      NavigationBar(containerColor = FixTheme.colors.surface) {
        CustomerTab.entries.forEach { entry ->
          NavigationBarItem(
            selected = tab == entry,
            onClick = { tab = entry },
            icon = { Icon(entry.icon, contentDescription = null) },
            label = { Text(entry.label) },
            colors =
              NavigationBarItemDefaults.colors(
                selectedIconColor = FixTheme.colors.primary,
                selectedTextColor = FixTheme.colors.primary,
                unselectedIconColor = FixTheme.colors.textSecondary,
                unselectedTextColor = FixTheme.colors.textSecondary,
                indicatorColor = FixTheme.colors.primarySurface,
              ),
          )
        }
      }
    },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      when (tab) {
        CustomerTab.POST ->
          TaskFlowContainer(
            onBackToRoles = onBackToRoles,
            // Posting used to dead-end on "Return to Home". Landing on the list instead shows the
            // customer the thing they just created, which is the whole point of this batch.
            onTaskPosted = { tab = CustomerTab.MY_TASKS },
          )
        CustomerTab.MY_TASKS ->
          MyTasksScreen(
            tasks = tasks,
            onOpenTask = viewModel::openTask,
            onPostTask = { tab = CustomerTab.POST },
          )
      }
    }
  }
}
