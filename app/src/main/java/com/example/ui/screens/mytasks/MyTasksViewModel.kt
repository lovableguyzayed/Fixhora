package com.example.ui.screens.mytasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import com.example.data.repository.TaskRepository
import com.example.data.room.ChatMessageEntity
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.data.session.SessionManager
import com.example.ui.format.isCancellableByCustomer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The customer's own tasks, and the conversation on each one.
 *
 * Before this existed a posted task simply disappeared: the wizard ended on a success screen whose
 * only exit was "Return to Home", and nothing in the app ever showed the customer what they had
 * asked for or what became of it. Chat had the same shape — the worker could open a thread, the
 * customer had no way in at all, so every conversation was one-sided.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyTasksViewModel(
  private val taskRepository: TaskRepository,
  private val chatRepository: ChatRepository,
  sessionManager: SessionManager,
) : ViewModel() {

  /**
   * Whose tasks to show.
   *
   * Follows the session rather than being read once: signing in or out while the screen is open
   * must swap the list, not keep showing the previous account's tasks.
   */
  private val ownerId: StateFlow<String> =
    sessionManager.session
      .map { it.ownerId(TaskEntity.GUEST_OWNER_ID) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskEntity.GUEST_OWNER_ID)

  val tasks: StateFlow<List<TaskEntity>> =
    ownerId
      .flatMapLatest { taskRepository.tasksForOwner(it) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  /** Which task's detail is open, or null on the list. */
  private val _openTaskId = MutableStateFlow<Int?>(null)
  val openTaskId: StateFlow<Int?> = _openTaskId

  /**
   * The open task, read from the database rather than captured from the list.
   *
   * The worker can accept or complete it while the customer is looking at it, and the status has
   * to follow. Emits null when the row is gone, which is how the detail screen knows to close.
   */
  val openTask: StateFlow<TaskEntity?> =
    _openTaskId
      .flatMapLatest { id -> if (id == null) emptyFlow() else taskRepository.observeTask(id) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  val messages: StateFlow<List<ChatMessageEntity>> =
    _openTaskId
      .flatMapLatest { id -> if (id == null) emptyFlow() else chatRepository.getMessagesForTask(id) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

  fun openTask(taskId: Int) {
    _openTaskId.value = taskId
  }

  fun closeTask() {
    _openTaskId.value = null
  }

  /** Sends as the customer, so the worker's thread shows it on the other side. */
  fun sendMessage(text: String) {
    val taskId = _openTaskId.value ?: return
    val body = text.trim()
    if (body.isEmpty()) return
    viewModelScope.launch {
      chatRepository.sendMessage(taskId, ChatRepository.SENDER_CUSTOMER, body)
    }
  }

  /**
   * Calls the task off.
   *
   * Guarded on the current row rather than on what the list showed, because a worker may have
   * accepted it in the meantime — cancelling then would leave someone working on a task the
   * customer believes is dead.
   */
  fun cancelOpenTask() {
    val task = openTask.value ?: return
    if (!isCancellableByCustomer(task.status)) return
    viewModelScope.launch { taskRepository.updateTaskStatus(task, TaskStatus.CANCELLED) }
  }

  class Factory(
    private val taskRepository: TaskRepository,
    private val chatRepository: ChatRepository,
    private val sessionManager: SessionManager,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      MyTasksViewModel(taskRepository, chatRepository, sessionManager) as T
  }
}
