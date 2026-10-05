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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.R
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
            UpdatePhase.UPDATE_AVAILABLE -> stringResource(R.string.update_title_available)
            UpdatePhase.NEEDS_INSTALL_PERMISSION ->
              stringResource(R.string.update_title_permission)
            UpdatePhase.DOWNLOADING -> stringResource(R.string.update_title_downloading)
            UpdatePhase.READY_TO_INSTALL -> stringResource(R.string.update_title_ready)
            UpdatePhase.UP_TO_DATE -> stringResource(R.string.update_title_current)
            else -> stringResource(R.string.update_title_failed)
          },
        fontWeight = FontWeight.SemiBold,
      )
    },
    text = {
      Column {
        when (state.phase) {
          UpdatePhase.UPDATE_AVAILABLE ->
            Text(
              stringResource(
                R.string.update_body_available,
                release?.versionName.orEmpty(),
                BuildConfig.VERSION_NAME,
                release?.displaySize.orEmpty(),
              )
            )

          UpdatePhase.NEEDS_INSTALL_PERMISSION ->
            Text(
              stringResource(R.string.update_body_permission)
            )

          UpdatePhase.DOWNLOADING -> {
            val fraction = state.progressFraction
            Text(
              if (fraction == null) {
                stringResource(R.string.update_body_starting)
              } else {
                stringResource(
                  R.string.update_body_progress,
                  (fraction * 100).toInt(),
                  UpdateChannel.formatBytes(state.totalBytes),
                )
              }
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
              stringResource(R.string.update_body_ready)
            )

          UpdatePhase.UP_TO_DATE ->
            Text(stringResource(R.string.update_body_current, BuildConfig.VERSION_NAME))

          else ->
            Column {
              Text(state.failure?.text() ?: stringResource(R.string.update_body_failed_generic))
              Spacer(Modifier.height(Spacing.md))
              Text(
                text = stringResource(R.string.update_body_failed_hint),
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
            Text(stringResource(R.string.update_action_now), color = colors.primary, fontWeight = FontWeight.SemiBold)
          }

        UpdatePhase.NEEDS_INSTALL_PERMISSION ->
          TextButton(
            onClick = { viewModel.installPermissionIntent()?.let(permissionLauncher::launch) }
          ) {
            Text(stringResource(R.string.update_action_settings), color = colors.primary, fontWeight = FontWeight.SemiBold)
          }

        UpdatePhase.READY_TO_INSTALL ->
          TextButton(onClick = { viewModel.installIntent()?.let(context::startActivity) }) {
            Text(stringResource(R.string.update_action_install), color = colors.primary, fontWeight = FontWeight.SemiBold)
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
            Text(stringResource(R.string.update_action_releases), color = colors.primary, fontWeight = FontWeight.SemiBold)
          }

        else ->
          TextButton(onClick = viewModel::dismiss) {
            Text(stringResource(R.string.update_action_ok), color = colors.primary, fontWeight = FontWeight.SemiBold)
          }
      }
    },
    dismissButton = {
      // Downloading is the one phase where dismissing throws work away, so it says so.
      when (state.phase) {
        UpdatePhase.UP_TO_DATE -> Unit
        UpdatePhase.DOWNLOADING ->
          TextButton(onClick = viewModel::dismiss) { Text(stringResource(R.string.update_action_cancel), color = colors.textSecondary) }
        else ->
          TextButton(onClick = viewModel::dismiss) {
            Text(stringResource(R.string.update_action_not_now), color = colors.textSecondary)
          }
      }
    },
  )
}
