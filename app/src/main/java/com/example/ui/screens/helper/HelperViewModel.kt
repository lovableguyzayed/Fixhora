package com.example.ui.screens.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.repository.TaskRepository
import com.example.data.room.OfferEntity
import com.example.data.room.TaskEntity
import com.example.util.AppPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JobItem(val task: TaskEntity, val myOffer: OfferEntity?, val isOwn: Boolean)
data class MyJob(val offer: OfferEntity, val task: TaskEntity?)

class HelperViewModel(
    private val repo: TaskRepository,
    private val prefs: AppPrefs,
    val helperId: String,
    val helperName: String,
) : ViewModel() {

    init {
        // Sample jobs only in test (debug) builds; real users never see fake jobs.
        if (BuildConfig.DEBUG) viewModelScope.launch { repo.seedDemoTasksIfEmpty() }
    }

    private val _online = MutableStateFlow(prefs.helperOnline)
    val online: StateFlow<Boolean> = _online.asStateFlow()

    /** Open jobs (`null` = still loading). */
    val jobs: StateFlow<List<JobItem>?> = combine(repo.completedTasks, repo.allOffers) { tasks, offers ->
        tasks.map { t -> JobItem(t, offers.firstOrNull { it.taskId == t.id && it.helperId == helperId }, t.ownerId == helperId) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Offers this helper has sent, newest first, with their task. */
    val myJobs: StateFlow<List<MyJob>?> = combine(repo.allTasks, repo.allOffers) { tasks, offers ->
        offers.filter { it.helperId == helperId }.map { o -> MyJob(o, tasks.firstOrNull { it.id == o.taskId }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setOnline(on: Boolean) { prefs.helperOnline = on; _online.value = on }

    fun sendOffer(task: TaskEntity, amount: Long, message: String) = viewModelScope.launch {
        repo.sendOffer(OfferEntity(taskId = task.id, helperId = helperId, helperName = helperName, amount = amount, message = message.trim()))
    }

    class Factory(
        private val repo: TaskRepository, private val prefs: AppPrefs,
        private val helperId: String, private val helperName: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HelperViewModel(repo, prefs, helperId, helperName) as T
    }
}
