package com.example.ui.screens.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import com.example.data.repository.TaskRepository
import com.example.data.repository.UserRepository
import com.example.data.room.ChatMessageEntity
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HelperViewModel(
  private val repository: TaskRepository,
  private val chatRepository: ChatRepository,
  private val userRepository: UserRepository,
) : ViewModel() {

  private fun <T> Flow<T>.asState(initial: T): StateFlow<T> =
    stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), initial)

  /** Jobs still open for anyone to take. */
  val availableTasks: StateFlow<List<TaskEntity>> = repository.openTasks.asState(emptyList())

  /** Every posted task, whatever its state. */
  val allTasks: StateFlow<List<TaskEntity>> = repository.allPostedTasks.asState(emptyList())

  /**
   * Counts derived from the database.
   *
   * The dashboard previously showed ₹1,250 earnings, 142 jobs completed, a 4.9 rating and
   * 95/98/96% bars — none of which came from any data. These are the numbers the app can actually
   * stand behind.
   */
  val stats: StateFlow<HelperStats> =
    allTasks.map { it.toHelperStats() }.asState(HelperStats())

  /**
   * Display names for the accounts that posted the visible tasks, so a card can say who it is
   * from instead of the literal text "Customer Name".
   */
  val ownerNames: StateFlow<Map<String, String>> =
    allTasks
      .map { tasks -> tasks.map { it.ownerId }.distinct().sorted() }
      .distinctUntilChanged()
      .map { ids ->
        ids.mapNotNull { id -> userRepository.findById(id)?.let { id to it.fullName } }.toMap()
      }
      .asState(emptyMap())

  // ------------------------------------------------------------------ search vocabulary

  /**
   * Category names in the language currently on screen, keyed by id.
   *
   * Supplied by the UI because a ViewModel cannot resolve a string resource, and search has to
   * match the words the user can actually see. Empty until the first screen reports them, which
   * only means search falls back to the task's own fields for that instant.
   */
  private val _categoryTitles = MutableStateFlow<Map<String, String>>(emptyMap())

  fun onCategoryTitlesChanged(titles: Map<String, String>) {
    if (_categoryTitles.value != titles) _categoryTitles.value = titles
  }

  // ------------------------------------------------------------------ task list filtering

  private val _taskQuery = MutableStateFlow("")
  val taskQuery: StateFlow<String> = _taskQuery.asStateFlow()

  private val _selectedTab = MutableStateFlow(TaskTab.ALL)
  val selectedTab: StateFlow<TaskTab> = _selectedTab.asStateFlow()

  val filteredTasks: StateFlow<List<TaskEntity>> =
    combine(allTasks, _selectedTab, _taskQuery, _categoryTitles) { tasks, tab, query, titles ->
        tasks.filterFor(tab, query, titles)
      }
      .asState(emptyList())

  fun onTaskQueryChange(query: String) {
    _taskQuery.value = query
  }

  fun onTabSelected(tab: TaskTab) {
    _selectedTab.value = tab
  }

  // ------------------------------------------------------------------ map screen filtering

  private val _mapQuery = MutableStateFlow("")
  val mapQuery: StateFlow<String> = _mapQuery.asStateFlow()

  /** Null means "all categories". */
  private val _mapCategoryId = MutableStateFlow<String?>(null)
  val mapCategoryId: StateFlow<String?> = _mapCategoryId.asStateFlow()

  val mapTasks: StateFlow<List<TaskEntity>> =
    combine(availableTasks, _mapQuery, _mapCategoryId, _categoryTitles) {
        tasks,
        query,
        categoryId,
        titles ->
        tasks.filter {
          (categoryId == null || it.categoryId == categoryId) && it.matchesQuery(query, titles)
        }
      }
      .asState(emptyList())

  fun onMapQueryChange(query: String) {
    _mapQuery.value = query
  }

  fun onMapCategorySelected(categoryId: String?) {
    _mapCategoryId.value = categoryId
  }

  // ------------------------------------------------------------------ chat

  private val _chatQuery = MutableStateFlow("")
  val chatQuery: StateFlow<String> = _chatQuery.asStateFlow()

  val conversations: StateFlow<List<TaskEntity>> =
    combine(allTasks, _chatQuery, _categoryTitles) { tasks, query, titles ->
        tasks.conversations().filter { it.matchesQuery(query, titles) }
      }
      .asState(emptyList())

  fun onChatQueryChange(query: String) {
    _chatQuery.value = query
  }

  /**
   * Task whose conversation should be opened, set from a job card's "Chat" action so that button
   * does something instead of nothing. Cleared once the chat screen has consumed it.
   */
  private val _pendingChatTaskId = MutableStateFlow<Int?>(null)
  val pendingChatTaskId: StateFlow<Int?> = _pendingChatTaskId.asStateFlow()

  fun requestChat(taskId: Int) {
    _pendingChatTaskId.value = taskId
  }

  fun consumePendingChat() {
    _pendingChatTaskId.value = null
  }

  fun getChatMessages(taskId: Int): Flow<List<ChatMessageEntity>> =
    chatRepository.getMessagesForTask(taskId)

  fun sendMessage(taskId: Int, text: String) {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return
    viewModelScope.launch {
      chatRepository.sendMessage(taskId, ChatRepository.SENDER_WORKER, trimmed)
    }
  }

  // ------------------------------------------------------------------ task actions

  fun acceptTask(task: TaskEntity) = updateStatus(task, TaskStatus.ACCEPTED)

  fun rejectTask(task: TaskEntity) = updateStatus(task, TaskStatus.REJECTED)

  fun startTask(task: TaskEntity) = updateStatus(task, TaskStatus.IN_PROGRESS)

  fun completeTask(task: TaskEntity) = updateStatus(task, TaskStatus.COMPLETED)

  private fun updateStatus(task: TaskEntity, status: TaskStatus) {
    viewModelScope.launch { repository.updateTaskStatus(task, status) }
  }

  class Factory(
    private val repository: TaskRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(HelperViewModel::class.java)) {
        return HelperViewModel(repository, chatRepository, userRepository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }

  private companion object {
    const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
  }
}
