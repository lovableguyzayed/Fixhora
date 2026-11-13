package com.example.ui.screens.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.market.MarketRepository
import com.example.data.market.Offer
import com.example.data.market.Task
import com.example.data.repository.TaskRepository
import com.example.util.AppPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JobItem(val task: Task, val myOffer: Offer?, val isOwn: Boolean)
data class MyJob(val offer: Offer, val task: Task?)

class HelperViewModel(
    private val market: MarketRepository,
    localTasks: TaskRepository,
    private val prefs: AppPrefs,
    val helperId: String,
    val helperName: String,
) : ViewModel() {

    init {
        // Sample jobs only in test (debug) builds without a server; real users never see fake jobs.
        if (BuildConfig.DEBUG && !market.isShared) viewModelScope.launch { localTasks.seedDemoTasksIfEmpty() }
    }

    private val _online = MutableStateFlow(prefs.helperOnline)
    val online: StateFlow<Boolean> = _online.asStateFlow()

    /** Offers this helper has sent, newest first, with their task (`null` = still loading). */
    val myJobs: StateFlow<List<MyJob>?> = market.offersSentBy(helperId)
        .map { pairs -> pairs.map { (o, t) -> MyJob(o, t) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Open jobs (`null` = still loading). */
    val jobs: StateFlow<List<JobItem>?> = combine(market.openJobs(), market.offersSentBy(helperId)) { tasks, mine ->
        tasks.map { t -> JobItem(t, mine.firstOrNull { it.first.taskId == t.id }?.first, t.ownerId == helperId) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setOnline(on: Boolean) { prefs.helperOnline = on; _online.value = on }

    fun sendOffer(task: Task, amount: Long, message: String) = viewModelScope.launch {
        market.sendOffer(Offer(id = "", taskId = task.id, helperId = helperId, helperName = helperName, amount = amount, message = message.trim()))
    }

    class Factory(
        private val market: MarketRepository, private val localTasks: TaskRepository, private val prefs: AppPrefs,
        private val helperId: String, private val helperName: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HelperViewModel(market, localTasks, prefs, helperId, helperName) as T
    }
}
