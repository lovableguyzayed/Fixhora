package com.example.ui.screens.taskflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.TaskRepository
import com.example.data.room.TaskEntity
import com.example.data.room.TaskStatus
import com.example.data.room.joinPhotoUris
import com.example.data.room.photoUriList
import com.example.data.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
  val latitude: Double? = null,
  val longitude: Double? = null,
  val selectedDistance: Int = 5,
  val descriptionTitle: String = "",
  val descriptionDetails: String = "",
  val minBudget: String = "",
  val maxBudget: String = "",
  val photoUris: List<String> = emptyList(),
)

class TaskViewModel(
  private val repository: TaskRepository,
  private val sessionManager: SessionManager,
) : ViewModel() {

  private val _uiState = MutableStateFlow(TaskDraftState())
  val uiState: StateFlow<TaskDraftState> = _uiState.asStateFlow()

  /** Whose draft this is. Null until the session has been read for the first time. */
  private var ownerId: String? = null

  private var saveJob: Job? = null

  init {
    viewModelScope.launch {
      sessionManager.session.collect { session ->
        val resolved = session.ownerId(TaskEntity.GUEST_OWNER_ID)
        if (resolved != ownerId) {
          // Signing in or out swaps which draft belongs to this device, so reload rather than
          // keeping the previous account's in-progress task on screen.
          ownerId = resolved
          loadDraft(resolved)
        }
      }
    }
  }

  private suspend fun loadDraft(forOwnerId: String) {
    try {
      val draft = repository.getDraftTask(forOwnerId)
      _uiState.value =
        if (draft == null) {
          TaskDraftState()
        } else {
          TaskDraftState(
            categoryId = draft.categoryId,
            locationQuery = draft.locationQuery,
            useCurrentLocation = draft.useCurrentLocation,
            latitude = draft.latitude,
            longitude = draft.longitude,
            selectedDistance = draft.selectedDistance,
            descriptionTitle = draft.descriptionTitle,
            descriptionDetails = draft.descriptionDetails,
            minBudget = draft.minBudget,
            maxBudget = draft.maxBudget,
            photoUris = draft.photoUriList(),
          )
        }
    } catch (e: Exception) {
      // A failed draft restore must not block task creation; the user simply starts fresh.
      e.printStackTrace()
    }
  }

  private fun toEntity(state: TaskDraftState, status: TaskStatus) =
    TaskEntity(
      status = status,
      ownerId = ownerId ?: TaskEntity.GUEST_OWNER_ID,
      categoryId = state.categoryId,
      locationQuery = state.locationQuery,
      useCurrentLocation = state.useCurrentLocation,
      latitude = state.latitude,
      longitude = state.longitude,
      selectedDistance = state.selectedDistance,
      descriptionTitle = state.descriptionTitle,
      descriptionDetails = state.descriptionDetails,
      minBudget = state.minBudget,
      maxBudget = state.maxBudget,
      photoUris = state.photoUris.joinPhotoUris(),
    )

  /** Debounced so that typing does not queue a database write per keystroke. */
  private fun saveDraft() {
    val currentState = _uiState.value
    saveJob?.cancel()
    saveJob =
      viewModelScope.launch {
        delay(AUTOSAVE_DEBOUNCE_MS)
        try {
          repository.saveDraft(toEntity(currentState, TaskStatus.DRAFT))
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
  }

  fun submitTask() {
    val currentState = _uiState.value
    // A debounced autosave still in flight would otherwise land after the draft has been
    // promoted and recreate it, leaving the user with a stale draft they never asked for.
    saveJob?.cancel()
    viewModelScope.launch {
      try {
        repository.submitTask(toEntity(currentState, TaskStatus.SUBMITTED))
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
    // Transient UI filter, not part of the task: deliberately not persisted.
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

  fun updateResolvedCoordinates(latitude: Double?, longitude: Double?) {
    _uiState.update { it.copy(latitude = latitude, longitude = longitude) }
    saveDraft()
  }

  fun updateSelectedDistance(distance: Int) {
    _uiState.update { it.copy(selectedDistance = distance) }
    saveDraft()
  }

  fun addPhotoUri(uri: String) {
    _uiState.update { if (uri in it.photoUris) it else it.copy(photoUris = it.photoUris + uri) }
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

  class Factory(
    private val repository: TaskRepository,
    private val sessionManager: SessionManager,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
        return TaskViewModel(repository, sessionManager) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }

  private companion object {
    const val AUTOSAVE_DEBOUNCE_MS = 500L
  }
}
