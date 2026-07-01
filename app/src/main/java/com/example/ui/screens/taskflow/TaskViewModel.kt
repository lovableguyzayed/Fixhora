package com.example.ui.screens.taskflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TaskRepository
import com.example.data.room.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDraftState(
    val categoryId: String? = null,
    val searchCategoryQuery: String = "",
    val locationQuery: String = "",
    val useCurrentLocation: Boolean = true,
    val selectedDistance: Int = 5,
    val descriptionTitle: String = "",
    val descriptionDetails: String = "",
    val minBudget: String = "",
    val maxBudget: String = "",
    val photoUris: List<String> = emptyList()
)

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskDraftState())
    val uiState: StateFlow<TaskDraftState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.insertDummyData()
        }
        viewModelScope.launch {
            try {
                val draft = repository.getDraftTask()
                if (draft != null) {
                    _uiState.value = TaskDraftState(
                        categoryId = draft.categoryId,
                        locationQuery = draft.locationQuery,
                        useCurrentLocation = draft.useCurrentLocation,
                        selectedDistance = draft.selectedDistance,
                        descriptionTitle = draft.descriptionTitle,
                        descriptionDetails = draft.descriptionDetails,
                        minBudget = draft.minBudget,
                        maxBudget = draft.maxBudget,
                        photoUris = if (draft.photoUris.isNotBlank()) draft.photoUris.split(",") else emptyList()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var saveJob: kotlinx.coroutines.Job? = null

    private fun saveDraft() {
        val currentState = _uiState.value
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            try {
                repository.saveDraft(
                    TaskEntity(
                        status = "draft",
                        categoryId = currentState.categoryId,
                        locationQuery = currentState.locationQuery,
                        useCurrentLocation = currentState.useCurrentLocation,
                        selectedDistance = currentState.selectedDistance,
                        descriptionTitle = currentState.descriptionTitle,
                        descriptionDetails = currentState.descriptionDetails,
                        minBudget = currentState.minBudget,
                        maxBudget = currentState.maxBudget,
                        photoUris = currentState.photoUris.joinToString(",")
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitTask() {
        val currentState = _uiState.value
        viewModelScope.launch {
            try {
                repository.submitTask(
                    TaskEntity(
                        status = "submitted",
                        categoryId = currentState.categoryId,
                        locationQuery = currentState.locationQuery,
                        useCurrentLocation = currentState.useCurrentLocation,
                        selectedDistance = currentState.selectedDistance,
                        descriptionTitle = currentState.descriptionTitle,
                        descriptionDetails = currentState.descriptionDetails,
                        minBudget = currentState.minBudget,
                        maxBudget = currentState.maxBudget,
                        photoUris = currentState.photoUris.joinToString(",")
                    )
                )
                clearDraft()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateCategory(categoryId: String) {
        _uiState.update { it.copy(categoryId = categoryId) }
        saveDraft()
    }

    fun updateSearchCategoryQuery(query: String) {
        _uiState.update { it.copy(searchCategoryQuery = query) }
    }

    fun updateLocationQuery(query: String) {
        _uiState.update { it.copy(locationQuery = query) }
        saveDraft()
    }

    fun updateUseCurrentLocation(use: Boolean) {
        _uiState.update { it.copy(useCurrentLocation = use) }
        saveDraft()
    }

    fun updateSelectedDistance(distance: Int) {
        _uiState.update { it.copy(selectedDistance = distance) }
        saveDraft()
    }

    fun addPhotoUri(uri: String) {
        _uiState.update { it.copy(photoUris = it.photoUris + uri) }
        saveDraft()
    }

    fun removePhotoUri(uri: String) {
        _uiState.update { it.copy(photoUris = it.photoUris - uri) }
        saveDraft()
    }
    
    fun updateDescription(title: String, details: String) {
        _uiState.update { it.copy(descriptionTitle = title, descriptionDetails = details) }
        saveDraft()
    }
    
    fun updateBudget(min: String, max: String) {
        _uiState.update { it.copy(minBudget = min, maxBudget = max) }
        saveDraft()
    }
    
    fun clearDraft() {
        _uiState.value = TaskDraftState()
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                return TaskViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
