package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Photo-picker links stop working after the app restarts, so picked photos are copied into the
 * app's private storage. No storage permission is needed.
 */
object PhotoStore {
    private const val DIR = "task_photos"

    /** Returns a file:// link to the private copy, or null if the photo could not be read/saved. */
    suspend fun copyToAppStorage(context: Context, source: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, DIR).apply { mkdirs() }
            val target = File(dir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            Uri.fromFile(target).toString()
        } catch (e: Exception) { // e.g. storage full, photo deleted meanwhile
            null
        }
    }

    /** Deletes a private copy made by [copyToAppStorage]; ignores anything else. */
    suspend fun delete(context: Context, link: String) = withContext(Dispatchers.IO) {
        val path = link.toUri().path ?: return@withContext
        val file = File(path)
        if (file.parentFile == File(context.filesDir, DIR)) file.delete()
    }
}
