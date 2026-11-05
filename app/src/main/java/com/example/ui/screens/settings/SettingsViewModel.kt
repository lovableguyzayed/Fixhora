package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.media.TaskPhotoStore
import com.example.data.repository.AccountRepository
import com.example.data.repository.UserRepository
import com.example.data.session.AppLanguage
import com.example.data.session.SessionManager
import com.example.data.session.ThemePreference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the settings screen shows. A null [name] means nobody is signed in. */
data class SettingsUiState(
  val name: String? = null,
  val mobile: String? = null,
  val city: String? = null,
  val language: AppLanguage = AppLanguage.ENGLISH,
  val theme: ThemePreference = ThemePreference.SYSTEM,
  val isDeleting: Boolean = false,
  val deleteFailed: Boolean = false,
) {
  val isSignedIn: Boolean
    get() = name != null
}

private data class DeleteState(val inProgress: Boolean = false, val failed: Boolean = false)

class SettingsViewModel(
  private val userRepository: UserRepository,
  private val sessionManager: SessionManager,
  private val accountRepository: AccountRepository,
  private val photoStore: TaskPhotoStore,
) : ViewModel() {

  private val deleteState = MutableStateFlow(DeleteState())

  @OptIn(ExperimentalCoroutinesApi::class)
  val state: StateFlow<SettingsUiState> =
    sessionManager.session
      .flatMapLatest { session ->
        val base = SettingsUiState(language = session.language, theme = session.theme)
        val userId = session.userId ?: return@flatMapLatest flowOf(base)
        userRepository.observeUser(userId).map { user ->
          if (user == null) {
            base
          } else {
            base.copy(name = user.fullName, mobile = user.mobile, city = user.city.ifBlank { null })
          }
        }
      }
      .combine(deleteState) { ui, delete -> ui.copy(isDeleting = delete.inProgress, deleteFailed = delete.failed) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch { sessionManager.setLanguage(language) }
  }

  fun setTheme(theme: ThemePreference) {
    viewModelScope.launch { sessionManager.setTheme(theme) }
  }

  fun signOut(onSignedOut: () -> Unit) {
    viewModelScope.launch {
      sessionManager.signOut()
      onSignedOut()
    }
  }

  /**
   * Deletes the signed-in account and everything it posted, then signs out.
   *
   * If the database step fails nothing has been removed (it is one transaction), so the user stays
   * signed in and is told, rather than being signed out of an account that still exists.
   */
  fun deleteAccount(onDeleted: () -> Unit) {
    if (deleteState.value.inProgress) return
    deleteState.value = DeleteState(inProgress = true)
    viewModelScope.launch {
      val userId = sessionManager.current().userId
      val photos =
        try {
          userId?.let { accountRepository.deleteAccount(it) }.orEmpty()
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          deleteState.value = DeleteState(failed = true)
          return@launch
        }
      photoStore.deleteAll(photos)
      sessionManager.signOut()
      deleteState.update { DeleteState() }
      onDeleted()
    }
  }

  fun dismissDeleteError() {
    deleteState.update { it.copy(failed = false) }
  }

  class Factory(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val accountRepository: AccountRepository,
    private val photoStore: TaskPhotoStore,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      SettingsViewModel(userRepository, sessionManager, accountRepository, photoStore) as T
  }
}
