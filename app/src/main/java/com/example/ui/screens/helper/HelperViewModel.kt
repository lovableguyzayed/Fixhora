package com.example.ui.screens.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TaskRepository
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

import kotlinx.coroutines.launch

import com.example.data.repository.ChatRepository
import com.example.data.room.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

class HelperViewModel(
    private val repository: TaskRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            // Sample jobs only in test (debug) builds; real users never see fake jobs.
            if (com.example.BuildConfig.DEBUG) repository.seedDemoTasksIfEmpty()
        }
    }

    /** Jobs open for helpers. `null` means "still loading" (so screens can show a skeleton). */
    val availableTasks: StateFlow<List<TaskEntity>?> = repository.completedTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun acceptTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, "accepted")
        }
    }

    fun rejectTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, "rejected")
        }
    }
    
    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, "completed")
        }
    }

    fun getChatMessages(taskId: Int): Flow<List<ChatMessageEntity>> = chatRepository.getMessagesForTask(taskId)

    fun sendMessage(taskId: Int, text: String) {
        viewModelScope.launch {
            chatRepository.sendMessage(taskId, "worker", text)
        }
    }

    class Factory(private val repository: TaskRepository, private val chatRepository: ChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HelperViewModel::class.java)) {
                return HelperViewModel(repository, chatRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
