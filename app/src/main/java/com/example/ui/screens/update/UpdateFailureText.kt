package com.example.ui.screens.update

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.update.UpdateFailure

/**
 * What each [UpdateFailure] says to the user.
 *
 * Split from the enum for the same reason as [com.example.ui.format.TaskFormattingText]: the
 * decision of *which* failure happened is pure Kotlin and unit-tested on the JVM, while the words
 * belong to the resource system so that they follow the app's language.
 */
@Composable
fun UpdateFailure.text(): String =
  stringResource(
    when (this) {
      UpdateFailure.NO_NETWORK -> R.string.update_fail_no_network
      UpdateFailure.RATE_LIMITED -> R.string.update_fail_rate_limited
      UpdateFailure.SERVER_ERROR -> R.string.update_fail_server
      UpdateFailure.MALFORMED_RESPONSE -> R.string.update_fail_malformed
      UpdateFailure.NO_ASSET_FOR_CHANNEL -> R.string.update_fail_no_asset
      UpdateFailure.DOWNLOAD_FAILED -> R.string.update_fail_download
    }
  )
