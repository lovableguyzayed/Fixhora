package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.market.MarketRepository
import com.example.data.market.Offer
import com.example.data.market.Task
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskWithOffers(val task: Task, val offers: List<Offer>) {
    val chosen get() = offers.firstOrNull { it.status == "accepted" }
    val openOffers get() = offers.filter { it.status == "sent" }.sortedBy { it.amount }
}

class CustomerViewModel(private val market: MarketRepository, val ownerId: String) : ViewModel() {
    /** `null` while loading. */
    val myTasks: StateFlow<List<TaskWithOffers>?> =
        combine(market.tasksOwnedBy(ownerId), market.offersOnTasksOf(ownerId)) { tasks, offers ->
            tasks.map { t -> TaskWithOffers(t, offers.filter { it.taskId == t.id }) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun chooseOffer(t: TaskWithOffers, offer: Offer) = viewModelScope.launch { market.chooseOffer(t.task, offer, t.offers) }
    fun markDone(task: Task) = viewModelScope.launch { market.setStatus(task, "completed") }
    fun cancel(task: Task) = viewModelScope.launch { market.setStatus(task, "cancelled") }

    class Factory(private val market: MarketRepository, private val ownerId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CustomerViewModel(market, ownerId) as T
    }
}
