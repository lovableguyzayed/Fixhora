package com.example.ui.screens.update

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.data.update.UpdateChannel
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Spacing

/**
 * The whole update conversation, in one dialog.
 *
 * A dialog rather than an inline banner because it has to work over every screen in the app —
 * splash, the task flow, the worker tabs — without any of them changing their layout to make room
 * for it. Nothing is shown in [UpdatePhase.IDLE], so the normal case costs the user nothing.
 */
@Composable
fun UpdateDialog(viewModel: UpdateViewModel) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val colors = FixTheme.colors

  // Returning from the "Install unknown apps" screen: the result code says nothing useful, so the
  // permission itself is re-read instead.
  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      viewModel.retryAfterPermission()
    }

  // Opens the installer as soon as the file lands, keyed on the file so it fires once per download
  // rather than on every recomposition. The button below covers the case where the user backs out
  // of the system installer and wants it again.
  LaunchedEffect(state.downloadedFile) {
    if (state.phase == UpdatePhase.READY_TO_INSTALL) {
      viewModel.installIntent()?.let(context::startActivity)
    }
  }

  when (state.phase) {
    UpdatePhase.IDLE,
    UpdatePhase.CHECKING -> return
    else -> Unit
  }

  val release = state.release

  AlertDialog(
    onDismissRequest = viewModel::dismiss,
    containerColor = colors.surface,
    titleContentColor = colors.textPrimary,
    textContentColor = colors.textSecondary,
    title = {
      Text(
        text =
          when (state.phase) {
            UpdatePhase.UPDATE_AVAILABLE -> "Update available"
            UpdatePhase.NEEDS_INSTALL_PERMISSION -> "One permission needed"
            UpdatePhase.DOWNLOADING -> "Downloading update"
            UpdatePhase.READY_TO_INSTALL -> "Ready to install"
            UpdatePhase.UP_TO_DATE -> "You are up to date"
            else -> "Update check failed"
          },
        fontWeight = FontWeight.SemiBold,
      )
    },
    text = {
      Column {
        when (state.phase) {
          UpdatePhase.UPDATE_AVAILABLE ->
            Text(
              "Version ${release?.versionName.orEmpty()} is available. " +
                "You are on ${BuildConfig.VERSION_NAME}.\n\n" +
                "Download size: ${release?.displaySize.orEmpty()}. " +
                "Your account and posted tasks stay as they are."
            )

          UpdatePhase.NEEDS_INSTALL_PERMISSION ->
            Text(
              "Android needs your permission before this app can install its own updates. " +
                "Turn on \"Allow from this source\" on the next screen, then come back — " +
                "the download will start on its own."
            )

          UpdatePhase.DOWNLOADING -> {
            val fraction = state.progressFraction
            Text(
              if (fraction == null) "Starting download…"
              else "${(fraction * 100).toInt()}% of ${UpdateChannel.formatBytes(state.totalBytes)}"
            )
            Spacer(Modifier.height(Spacing.md))
            if (fraction == null) {
              LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = colors.primary,
                trackColor = colors.surfaceAlt,
              )
            } else {
              LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth(),
                color = colors.primary,
                trackColor = colors.surfaceAlt,
              )
            }
          }

          UpdatePhase.READY_TO_INSTALL ->
            Text(
              "The installer should be open. If it is not, tap Install below and confirm the " +
                "system prompt."
            )

          UpdatePhase.UP_TO_DATE ->
            Text("${BuildConfig.VERSION_NAME} is the newest build published.")

          else ->
            Column {
              Text(state.failure?.message ?: "Something went wrong while checking for updates.")
              Spacer(Modifier.height(Spacing.md))
              Text(
                text = "You can always download the APK from the releases page instead.",
                fontSize = 13.sp,
                color = colors.textMuted,
              )
            }
        }
      }
    },
    confirmButton = {
      when (state.phase) {
        UpdatePhase.UPDATE_AVAILABLE ->
          TextButton(onClick = viewModel::startDownload) {
            Text("Update now", color = colors.primary, fontWeight = FontWeight.SemiBold)
          }

        UpdatePhase.NEEDS_INSTALL_PERMISSION ->
          TextButton(
            onClick = { viewModel.installPermissionIntent()?.let(permissionLauncher::launch) }
          ) {
            Text("Open settings", color = colors.primary, fontWeight = FontWeight.SemiBold)
          }

        UpdatePhase.READY_TO_INSTALL ->
          TextButton(onClick = { viewModel.installIntent()?.let(context::startActivity) }) {
            Text("Install", color = colors.primary, fontWeight = FontWeight.SemiBold)
          }

        UpdatePhase.FAILED ->
          TextButton(
            onClick = {
              context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(UpdateChannel.RELEASES_PAGE))
              )
              viewModel.dismiss()
            }
          ) {
            Text("Open releases", color = colors.primary, fontWeight = FontWeight.SemiBold)
          }

        else ->
          TextButton(onClick = viewModel::dismiss) {
            Text("OK", color = colors.primary, fontWeight = FontWeight.SemiBold)
          }
      }
    },
    dismissButton = {
      // Downloading is the one phase where dismissing throws work away, so it says so.
      when (state.phase) {
        UpdatePhase.UP_TO_DATE -> Unit
        UpdatePhase.DOWNLOADING ->
          TextButton(onClick = viewModel::dismiss) { Text("Cancel", color = colors.textSecondary) }
        else ->
          TextButton(onClick = viewModel::dismiss) {
            Text("Not now", color = colors.textSecondary)
          }
      }
    },
  )
}
