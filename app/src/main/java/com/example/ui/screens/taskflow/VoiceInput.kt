package com.example.ui.screens.taskflow

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.components.PillChip

/**
 * "Speak instead of typing", for the task description.
 *
 * Many people the app is for find it far easier to say what is wrong than to type it, in Hindi
 * especially. This hands off to the phone's own speech recogniser, so there is nothing to download
 * and no audio passes through the app. The button is left out on a phone without one.
 */
@Composable
fun VoiceInputButton(onText: (String) -> Unit, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val available = remember { context.hasSpeechRecognizer() }
  if (!available) return

  val locale = LocalConfiguration.current.locales[0]
  val languageTag = if (locale.language == "hi") "hi-IN" else "en-IN"
  val prompt = stringResource(R.string.voice_input)
  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        result.data
          ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
          ?.firstOrNull()
          ?.trim()
          ?.takeIf { it.isNotEmpty() }
          ?.let(onText)
      }
    }

  PillChip(
    text = prompt,
    selected = false,
    icon = Icons.Default.Mic,
    modifier = modifier,
    onClick = {
      val intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
          .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          .putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
          .putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
      try {
        launcher.launch(intent)
      } catch (e: ActivityNotFoundException) {
        // The recogniser went away between the check and the tap.
      }
    },
  )
}

/** Needs the `<queries>` entry in the manifest to see the recogniser on Android 11 and later. */
private fun Context.hasSpeechRecognizer(): Boolean =
  try {
    packageManager
      .queryIntentActivities(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), PackageManager.MATCH_DEFAULT_ONLY)
      .isNotEmpty()
  } catch (e: RuntimeException) {
    false
  }

/** Adds dictated text after what is already typed. */
fun appendDictation(existing: String, spoken: String): String =
  if (existing.isBlank()) spoken else existing.trimEnd() + " " + spoken
