package com.example.ui.screens.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.session.SessionManager
import com.example.data.update.ApkInstaller
import com.example.data.update.DownloadProgress
import com.example.data.update.ReleaseInfo
import com.example.data.update.UpdateFailure
import com.example.data.update.UpdateRepository
import com.example.data.update.UpdateStatus
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Six hours between automatic checks: often enough to catch a same-day build, quiet enough to ignore. */
private const val AUTO_CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L

/** Where the update flow currently stands. Drives exactly what the dialog shows. */
enum class UpdatePhase {
  /** Nothing to show. */
  IDLE,
  CHECKING,
  UPDATE_AVAILABLE,
  /** An update is ready to download but the system will not let this app install anything yet. */
  NEEDS_INSTALL_PERMISSION,
  DOWNLOADING,
  READY_TO_INSTALL,
  /** Only surfaced after a check the user asked for; an automatic one stays silent. */
  UP_TO_DATE,
  FAILED,
}

data class UpdateUiState(
  val phase: UpdatePhase = UpdatePhase.IDLE,
  val release: ReleaseInfo? = null,
  val downloadedBytes: Long = 0L,
  val totalBytes: Long = 0L,
  val failure: UpdateFailure? = null,
  val downloadedFile: File? = null,
) {
  /** 0f..1f, or null while the size is still unknown — the bar shows indeterminate until then. */
  val progressFraction: Float?
    get() = if (totalBytes > 0L) (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else null
}

/**
 * Drives the in-app update flow: check, download, install.
 *
 * Before this existed the app had no way to learn that a newer build had been published. The
 * releases were there, but nothing ever looked at them, so "over the air update" meant opening a
 * browser and remembering to check.
 */
class UpdateViewModel(
  private val repository: UpdateRepository,
  private val installer: ApkInstaller,
  private val sessionManager: SessionManager,
  private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

  private val _uiState = MutableStateFlow(UpdateUiState())
  val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

  private var downloadJob: Job? = null
  private var downloadId: Long? = null
  private var autoCheckStarted = false

  /**
   * The check that runs on launch.
   *
   * Runs at most once per composition and at most once every six hours, and says nothing when the
   * app is already current — an automatic check that reports "you are up to date" every time the
   * app opens is noise.
   */
  fun checkOnLaunch() {
    if (autoCheckStarted) return
    autoCheckStarted = true

    viewModelScope.launch {
      val elapsed = now() - sessionManager.lastUpdateCheckAt()
      if (elapsed < AUTO_CHECK_INTERVAL_MS) return@launch
      runCheck(announceUpToDate = false)
    }
  }

  /** The check the user asked for, from the version footer. Always runs, always answers. */
  fun checkNow() {
    if (_uiState.value.phase == UpdatePhase.CHECKING) return
    viewModelScope.launch { runCheck(announceUpToDate = true) }
  }

  private suspend fun runCheck(announceUpToDate: Boolean) {
    _uiState.update { it.copy(phase = UpdatePhase.CHECKING, failure = null) }

    when (val status = repository.check()) {
      is UpdateStatus.Available ->
        _uiState.update { it.copy(phase = UpdatePhase.UPDATE_AVAILABLE, release = status.release) }
      UpdateStatus.UpToDate ->
        _uiState.update {
          it.copy(phase = if (announceUpToDate) UpdatePhase.UP_TO_DATE else UpdatePhase.IDLE)
        }
      is UpdateStatus.Failed ->
        _uiState.update {
          // A failed automatic check stays out of the way; the user did not ask for it.
          if (announceUpToDate) it.copy(phase = UpdatePhase.FAILED, failure = status.failure)
          else it.copy(phase = UpdatePhase.IDLE)
        }
    }

    // Recorded whatever the outcome, so a repeatedly failing check does not retry on every launch.
    sessionManager.recordUpdateCheck(now())
  }

  /**
   * Starts the download, unless the system would refuse the install at the end of it.
   *
   * Checking the permission up front rather than after a 25 MB download is the difference between
   * one extra tap and a download the user has to repeat.
   */
  fun startDownload() {
    val release = _uiState.value.release ?: return
    if (downloadJob != null) return

    if (!installer.canInstallPackages()) {
      _uiState.update { it.copy(phase = UpdatePhase.NEEDS_INSTALL_PERMISSION) }
      return
    }

    val id = installer.enqueue(release)
    if (id == null) {
      _uiState.update {
        it.copy(phase = UpdatePhase.FAILED, failure = UpdateFailure.DOWNLOAD_FAILED)
      }
      return
    }

    downloadId = id
    _uiState.update {
      it.copy(phase = UpdatePhase.DOWNLOADING, downloadedBytes = 0L, totalBytes = release.sizeBytes)
    }

    downloadJob =
      viewModelScope.launch {
        installer.observe(id).collect { progress ->
          when (progress) {
            is DownloadProgress.Running ->
              _uiState.update {
                it.copy(
                  downloadedBytes = progress.downloadedBytes,
                  // The server's length wins once known; the release size is only a starting guess.
                  totalBytes = if (progress.totalBytes > 0L) progress.totalBytes else it.totalBytes,
                )
              }
            is DownloadProgress.Done ->
              _uiState.update {
                it.copy(phase = UpdatePhase.READY_TO_INSTALL, downloadedFile = progress.file)
              }
            DownloadProgress.Failed ->
              _uiState.update {
                it.copy(phase = UpdatePhase.FAILED, failure = UpdateFailure.DOWNLOAD_FAILED)
              }
          }
        }
        downloadJob = null
      }
  }

  /** The intent that opens this app's "Install unknown apps" switch, or null below Android 8. */
  fun installPermissionIntent() = installer.installPermissionIntent()

  /** The intent that opens the system installer for the downloaded APK. */
  fun installIntent() = _uiState.value.downloadedFile?.let(installer::installIntent)

  /** Called after returning from the "Install unknown apps" screen, to retry the download. */
  fun retryAfterPermission() {
    if (!installer.canInstallPackages()) return
    _uiState.update { it.copy(phase = UpdatePhase.UPDATE_AVAILABLE) }
    startDownload()
  }

  /** Closes the dialog. An in-flight download is cancelled rather than left running unseen. */
  fun dismiss() {
    downloadJob?.cancel()
    downloadJob = null
    downloadId?.let(installer::cancel)
    downloadId = null
    _uiState.value = UpdateUiState()
  }

  class Factory(
    private val repository: UpdateRepository,
    private val installer: ApkInstaller,
    private val sessionManager: SessionManager,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      UpdateViewModel(repository, installer, sessionManager) as T
  }
}
