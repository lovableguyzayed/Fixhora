package com.example.data.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** How often the download is polled for progress. Fast enough to feel live, cheap enough to ignore. */
private const val POLL_INTERVAL_MS = 400L

private const val APK_MIME_TYPE = "application/vnd.android.package-archive"

/** Where downloads land, and the prefix used to recognise our own leftovers. */
private const val APK_NAME_PREFIX = "fixhora-"

/**
 * Downloads a release APK and hands it to the system installer.
 *
 * Uses [DownloadManager] rather than a hand-rolled download: it survives the app being backgrounded,
 * resumes across connectivity changes, and shows progress in the notification shade for free.
 *
 * Files go to the app-specific external directory, which needs no storage permission on any
 * supported API level and is cleaned up when the app is uninstalled.
 */
class ApkInstaller(context: Context) {

  private val appContext = context.applicationContext

  private val downloadManager: DownloadManager
    get() = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

  private val downloadDir: File?
    get() = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

  /**
   * Whether the system will let this app start an install at all.
   *
   * From Android 8 every installing app needs its own "Install unknown apps" grant. This is the
   * most common reason a sideloaded update silently does nothing: the download finishes, the
   * installer opens, and the system closes it again. Checking first means the UI can ask for the
   * grant instead of looking broken.
   */
  fun canInstallPackages(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
      appContext.packageManager.canRequestPackageInstalls()

  /** Takes the user straight to this app's own "Install unknown apps" switch. */
  fun installPermissionIntent(): Intent? =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      null
    } else {
      Intent(
          Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
          Uri.parse("package:${appContext.packageName}"),
        )
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

  /**
   * Queues the download and returns its id, or null when there is nowhere to write.
   *
   * Any previously downloaded APK is removed first: only one is ever useful, and a 25 MB file per
   * update would otherwise accumulate for the life of the install.
   */
  fun enqueue(release: ReleaseInfo): Long? {
    val directory = downloadDir ?: return null
    deletePreviousDownloads(directory)

    val fileName = downloadFileName(release)
    val request =
      DownloadManager.Request(Uri.parse(release.apkUrl))
        .setTitle("FixoraX ${release.versionName}")
        .setDescription("Downloading update")
        .setMimeType(APK_MIME_TYPE)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        .setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, fileName)

    return runCatching { downloadManager.enqueue(request) }.getOrNull()
  }

  /**
   * Emits progress until the download reaches a terminal state, then stops.
   *
   * Polled rather than driven by `ACTION_DOWNLOAD_COMPLETE`: a broadcast receiver would have to be
   * registered with an export flag from Android 14, unregistered on every exit path, and it still
   * would not report progress. A 400 ms query on a cursor costs less than getting that wrong.
   */
  fun observe(downloadId: Long): Flow<DownloadProgress> = flow {
    while (true) {
      val progress = snapshot(downloadId)
      emit(progress)
      if (progress !is DownloadProgress.Running) return@flow
      delay(POLL_INTERVAL_MS)
    }
  }

  /** Cancels an in-flight download and discards its partial file. */
  fun cancel(downloadId: Long) {
    runCatching { downloadManager.remove(downloadId) }
  }

  /**
   * The intent that opens the system installer.
   *
   * The APK is handed over as a `content://` URI through [FileProvider] with a read grant; a
   * `file://` URI has thrown `FileUriExposedException` since Android 7.
   */
  fun installIntent(file: File): Intent {
    val uri =
      FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)
    return Intent(Intent.ACTION_VIEW)
      .setDataAndType(uri, APK_MIME_TYPE)
      .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }

  private fun snapshot(downloadId: Long): DownloadProgress {
    val cursor =
      runCatching { downloadManager.query(DownloadManager.Query().setFilterById(downloadId)) }
        .getOrNull() ?: return DownloadProgress.Failed

    return cursor.use {
      if (!it.moveToFirst()) return@use DownloadProgress.Failed

      val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
      val downloaded =
        it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
      val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

      when (status) {
        DownloadManager.STATUS_SUCCESSFUL -> {
          val localUri = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
          val path = localUri?.let(Uri::parse)?.path
          val file = path?.let(::File)
          if (file != null && file.exists()) DownloadProgress.Done(file) else DownloadProgress.Failed
        }
        DownloadManager.STATUS_FAILED -> DownloadProgress.Failed
        else -> DownloadProgress.Running(downloaded, total)
      }
    }
  }

  /** Only ever touches this app's own download directory, never anything the user put there. */
  private fun deletePreviousDownloads(directory: File) {
    directory
      .listFiles { file -> file.name.startsWith(APK_NAME_PREFIX) && file.name.endsWith(".apk") }
      ?.forEach { it.delete() }
  }

  /**
   * The name to save under: the asset's own file name, which already encodes the channel.
   *
   * Falls back to a constructed name if the URL has no usable last segment, because
   * `setDestinationInExternalFilesDir` needs one and a null would drop the download entirely.
   */
  private fun downloadFileName(release: ReleaseInfo): String {
    val fromUrl = release.apkUrl.substringAfterLast('/', "")
    return if (fromUrl.endsWith(".apk") && fromUrl.startsWith(APK_NAME_PREFIX)) {
      fromUrl
    } else {
      "$APK_NAME_PREFIX${release.versionName}.apk"
    }
  }
}

/** Where a queued download has got to. [Running] repeats; the other two end the stream. */
sealed interface DownloadProgress {
  data class Running(val downloadedBytes: Long, val totalBytes: Long) : DownloadProgress

  data class Done(val file: File) : DownloadProgress

  data object Failed : DownloadProgress
}
