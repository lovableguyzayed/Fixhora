package com.example.ui.screens.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import com.example.data.repository.TaskRepository
import com.example.data.room.ChatMessageEntity
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HelperViewModel(
  private val repository: TaskRepository,
  private val chatRepository: ChatRepository,
) : ViewModel() {

  /** Jobs still open for anyone to take. */
  val availableTasks: StateFlow<List<TaskEntity>> =
    repository.openTasks.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
      initialValue = emptyList(),
    )

  /** Every posted task, whatever its state, for the "My Tasks" screen. */
  val allTasks: StateFlow<List<TaskEntity>> =
    repository.allPostedTasks.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
      initialValue = emptyList(),
    )

  fun acceptTask(task: TaskEntity) {
    viewModelScope.launch { repository.updateTaskStatus(task, TaskStatus.ACCEPTED) }
  }

  fun rejectTask(task: TaskEntity) {
    viewModelScope.launch { repository.updateTaskStatus(task, TaskStatus.REJECTED) }
  }

  fun startTask(task: TaskEntity) {
    viewModelScope.launch { repository.updateTaskStatus(task, TaskStatus.IN_PROGRESS) }
  }

  fun completeTask(task: TaskEntity) {
    viewModelScope.launch { repository.updateTaskStatus(task, TaskStatus.COMPLETED) }
  }

  fun getChatMessages(taskId: Int): Flow<List<ChatMessageEntity>> =
    chatRepository.getMessagesForTask(taskId)

  fun sendMessage(taskId: Int, text: String) {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return
    viewModelScope.launch { chatRepository.sendMessage(taskId, ChatRepository.SENDER_WORKER, trimmed) }
  }

  class Factory(
    private val repository: TaskRepository,
    private val chatRepository: ChatRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(HelperViewModel::class.java)) {
        return HelperViewModel(repository, chatRepository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }

  private companion object {
    const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
  }
}
