package com.example.data.media

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Keeps a private copy of every photo attached to a task.
 *
 * The obvious alternative — storing the picker's `content://` URI and calling
 * `takePersistableUriPermission` — does not work here. The Android photo picker
 * (`ActivityResultContracts.PickVisualMedia`) grants read access only "until the device restarts
 * or your app stops", and those grants are not persistable, so the call throws `SecurityException`
 * on Android 13+ while succeeding on older devices that fall back to `ACTION_OPEN_DOCUMENT`.
 * Storing the URI is what made every attached photo render as a broken box after the app was
 * killed and reopened.
 *
 * Copying also means a task keeps its photos even if the user later deletes the original from
 * their gallery, which matters because a helper may look at the task days later.
 */
class TaskPhotoStore(context: Context) {

  private val appContext = context.applicationContext

  private val photoDirectory: File
    get() = File(appContext.filesDir, DIRECTORY_NAME).apply { mkdirs() }

  /**
   * Copies [uri] into app storage and returns the stored file's URI as a string, or null if the
   * source could not be read — a picked photo that has since been deleted, or a provider that
   * revoked access.
   */
  suspend fun import(uri: Uri): String? =
    withContext(Dispatchers.IO) {
      val target = File(photoDirectory, "${UUID.randomUUID()}.jpg")
      try {
        appContext.contentResolver.openInputStream(uri)?.use { input ->
          target.outputStream().use { output -> input.copyTo(output) }
        } ?: return@withContext null
        Uri.fromFile(target).toString()
      } catch (e: IOException) {
        target.delete()
        null
      } catch (e: SecurityException) {
        target.delete()
        null
      }
    }

  /** Removes a stored copy. Silently ignores anything this store did not write. */
  suspend fun delete(storedUri: String) {
    withContext(Dispatchers.IO) {
      val file = fileFor(storedUri) ?: return@withContext
      if (file.parentFile == photoDirectory) file.delete()
    }
  }

  suspend fun deleteAll(storedUris: List<String>) {
    storedUris.forEach { delete(it) }
  }

  private fun fileFor(storedUri: String): File? =
    try {
      Uri.parse(storedUri).path?.let(::File)
    } catch (e: Exception) {
      null
    }

  private companion object {
    const val DIRECTORY_NAME = "task_photos"
  }
}
