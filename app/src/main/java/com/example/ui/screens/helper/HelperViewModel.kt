package com.example.ui.screens.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TaskRepository
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            repository.insertDummyData()
        }
    }

    val availableTasks: StateFlow<List<TaskEntity>> = repository.completedTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Conversation currently open in the Chat tab; shared so any tab can jump into a chat.
    private val _openChatTaskId = MutableStateFlow<Int?>(null)
    val openChatTaskId: StateFlow<Int?> = _openChatTaskId.asStateFlow()

    fun openChat(taskId: Int) {
        _openChatTaskId.value = taskId
    }

    fun closeChat() {
        _openChatTaskId.value = null
    }

    fun acceptTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, "accepted")
        }
    }

    /** A bid keeps the job reserved for this helper until the customer responds. */
    fun placeBid(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, "pending")
        }
    }

    fun rejectTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, "rejected")
        }
    }

    fun reopenTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, "submitted")
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
