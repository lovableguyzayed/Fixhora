package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TaskRepository
import com.example.data.room.OfferEntity
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskWithOffers(val task: TaskEntity, val offers: List<OfferEntity>) {
    val chosen get() = offers.firstOrNull { it.status == "accepted" }
    val openOffers get() = offers.filter { it.status == "sent" }
}

class CustomerViewModel(private val repo: TaskRepository, val ownerId: String) : ViewModel() {
    /** `null` while loading. */
    val myTasks: StateFlow<List<TaskWithOffers>?> =
        combine(repo.tasksOwnedBy(ownerId), repo.allOffers) { tasks, offers ->
            tasks.map { t -> TaskWithOffers(t, offers.filter { it.taskId == t.id }) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun chooseOffer(taskId: Int, offerId: Int) = viewModelScope.launch { repo.acceptOffer(taskId, offerId) }
    fun markDone(task: TaskEntity) = viewModelScope.launch { repo.updateTaskStatus(task, "completed") }
    fun cancel(task: TaskEntity) = viewModelScope.launch { repo.updateTaskStatus(task, "cancelled") }

    class Factory(private val repo: TaskRepository, private val ownerId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CustomerViewModel(repo, ownerId) as T
    }
}
