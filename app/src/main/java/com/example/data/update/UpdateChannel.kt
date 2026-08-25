package com.example.data.update

/**
 * Where the app looks for new builds, and how it recognises the one that belongs to it.
 *
 * Deliberately free of Android imports so every decision here is unit-testable. The parts that
 * cannot be — the HTTP call, the download, the install intent — live in [UpdateRepository] and
 * [ApkInstaller] and are kept as thin as possible around this file.
 *
 * The repository is public, so no token is needed and none is shipped inside the APK.
 */
object UpdateChannel {

  /**
   * The release list, not `/releases/latest`.
   *
   * `latest` is ordered by each release's `created_at`, which is the tagged commit's date — and
   * every tag CI has published so far points at the same commit, so all three share one timestamp
   * and the ordering is decided by a tie-break rather than by version. Reading the list and taking
   * the highest version code is correct whatever order GitHub returns, and it also lets a release
   * that is missing this channel's APK be skipped instead of dead-ending the check.
   */
  const val RELEASES_API =
    "https://api.github.com/repos/lovableguyzayed/Fixhora/releases?per_page=10"

  /** Shown as a fallback whenever the automatic path fails, so the user is never stuck. */
  const val RELEASES_PAGE = "https://github.com/lovableguyzayed/Fixhora/releases/latest"

  /**
   * Reads the version code out of a release tag.
   *
   * CI tags every build `v1.0.<versionCode>` where the code is the commit count, so the tag is the
   * one place both the APK and the release agree on. Anything that is not that exact shape returns
   * null rather than a guess — a wrong number here would either hide a real update or offer an
   * install Android will refuse.
   */
  fun versionCodeFromTag(tag: String): Int? {
    val parts = tag.trim().removePrefix("v").split(".")
    if (parts.size != 3) return null
    if (parts.any { it.isEmpty() || it.any { char -> !char.isDigit() } }) return null
    return parts[2].toIntOrNull()?.takeIf { it > 0 }
  }

  /** `"v1.0.14"` -> `"1.0.14"`, the versionName the release carries. */
  fun versionNameFromTag(tag: String): String = tag.trim().removePrefix("v")

  /**
   * The asset name this build is allowed to install, matching what the workflow uploads.
   *
   * Channels must not cross. A debug build is `com.fixhora.app.debug` signed with the committed
   * debug key; a release build is `com.fixhora.app` signed with the private key. Handing either
   * one the other's APK produces an install failure, not an update, so the channel is part of the
   * lookup rather than something the user can get wrong.
   */
  fun apkAssetName(versionName: String, debugChannel: Boolean): String =
    if (debugChannel) "fixhora-$versionName-debug.apk" else "fixhora-$versionName.apk"

  /**
   * Android only replaces an installed app when the incoming versionCode is strictly higher.
   * Equal or lower is refused, so offering it would send the user into a failed install.
   */
  fun isNewer(installedVersionCode: Int, latestVersionCode: Int): Boolean =
    latestVersionCode > installedVersionCode

  /**
   * Decides what to do with the releases that carry an APK for this channel.
   *
   * Takes the highest version code rather than the first entry, so the answer does not depend on
   * the order the API happened to return.
   */
  fun pickUpdate(candidates: List<ReleaseInfo>, installedVersionCode: Int): UpdateStatus {
    if (candidates.isEmpty()) return UpdateStatus.Failed(UpdateFailure.NO_ASSET_FOR_CHANNEL)
    val newest = candidates.maxBy { it.versionCode }
    return if (isNewer(installedVersionCode, newest.versionCode)) {
      UpdateStatus.Available(newest)
    } else {
      UpdateStatus.UpToDate
    }
  }

  /** Human-readable download size, so "Update now" says how much data it will cost. */
  fun formatBytes(bytes: Long): String =
    when {
      bytes <= 0L -> "unknown size"
      bytes < 1024L -> "$bytes B"
      bytes < 1024L * 1024L -> "${roundToOneDecimal(bytes / 1024.0)} KB"
      bytes < 1024L * 1024L * 1024L -> "${roundToOneDecimal(bytes / (1024.0 * 1024.0))} MB"
      else -> "${roundToOneDecimal(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
    }

  private fun roundToOneDecimal(value: Double): String {
    val scaled = kotlin.math.round(value * 10.0).toLong()
    return "${scaled / 10}.${scaled % 10}"
  }
}

/** A build published on the releases page that this app could install. */
data class ReleaseInfo(
  val versionCode: Int,
  val versionName: String,
  val apkUrl: String,
  val sizeBytes: Long,
) {
  val displaySize: String
    get() = UpdateChannel.formatBytes(sizeBytes)
}

/**
 * Why a check could not answer the question.
 *
 * Each case is distinct because "update check failed" tells the user nothing about whether to retry
 * now, wait, or go download the APK by hand. The wording lives in `UpdateFailureText.kt` so that
 * this file stays free of Android imports and keeps running as a plain JVM test.
 */
enum class UpdateFailure {
  NO_NETWORK,
  RATE_LIMITED,
  SERVER_ERROR,
  MALFORMED_RESPONSE,
  NO_ASSET_FOR_CHANNEL,
  DOWNLOAD_FAILED,
}

/** The outcome of asking the releases page what the newest build is. */
sealed interface UpdateStatus {
  data object UpToDate : UpdateStatus

  data class Available(val release: ReleaseInfo) : UpdateStatus

  data class Failed(val failure: UpdateFailure) : UpdateStatus
}
