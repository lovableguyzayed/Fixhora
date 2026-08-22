package com.example.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateChannelTest {

  /**
   * The tag is the only place the APK and the release agree on a version, so a wrong reading here
   * either hides a real update or offers one Android will refuse to install.
   */
  @Test
  fun `version code is read from the release tag`() {
    assertEquals(14, UpdateChannel.versionCodeFromTag("v1.0.14"))
    assertEquals(14, UpdateChannel.versionCodeFromTag("1.0.14"))
    assertEquals(9, UpdateChannel.versionCodeFromTag("  v1.0.9  "))
    assertEquals(142, UpdateChannel.versionCodeFromTag("v1.0.142"))
  }

  @Test
  fun `anything that is not the CI tag shape is rejected rather than guessed`() {
    assertEquals(null, UpdateChannel.versionCodeFromTag("v1.0"))
    assertEquals(null, UpdateChannel.versionCodeFromTag("v1.0.14.2"))
    assertEquals(null, UpdateChannel.versionCodeFromTag("v1.0.beta"))
    assertEquals(null, UpdateChannel.versionCodeFromTag("v1..14"))
    assertEquals(null, UpdateChannel.versionCodeFromTag("v1.0.-3"))
    assertEquals(null, UpdateChannel.versionCodeFromTag(""))
    // versionCode 0 is not a build the workflow can produce.
    assertEquals(null, UpdateChannel.versionCodeFromTag("v1.0.0"))
  }

  @Test
  fun `version name drops the tag prefix`() {
    assertEquals("1.0.14", UpdateChannel.versionNameFromTag("v1.0.14"))
    assertEquals("1.0.14", UpdateChannel.versionNameFromTag("1.0.14"))
  }

  /** Must match `.github/workflows/release-apk.yml`, which names the uploaded files. */
  @Test
  fun `asset name matches what CI uploads for each channel`() {
    assertEquals(
      "fixhora-1.0.14-debug.apk",
      UpdateChannel.apkAssetName("1.0.14", debugChannel = true),
    )
    assertEquals("fixhora-1.0.14.apk", UpdateChannel.apkAssetName("1.0.14", debugChannel = false))
  }

  /** Android replaces an app only for a strictly higher versionCode; equal or lower is refused. */
  @Test
  fun `only a strictly higher version counts as an update`() {
    assertTrue(UpdateChannel.isNewer(installedVersionCode = 14, latestVersionCode = 15))
    assertTrue(UpdateChannel.isNewer(installedVersionCode = 1, latestVersionCode = 14))
    assertFalse(UpdateChannel.isNewer(installedVersionCode = 14, latestVersionCode = 14))
    assertFalse(UpdateChannel.isNewer(installedVersionCode = 14, latestVersionCode = 13))
  }

  private fun release(code: Int) =
    ReleaseInfo(
      versionCode = code,
      versionName = "1.0.$code",
      apkUrl = "https://example.invalid/fixhora-1.0.$code-debug.apk",
      sizeBytes = 25_561_597,
    )

  /**
   * The releases API is not guaranteed to return newest first — every tag CI has published points
   * at the same commit, so they share a `created_at` and the order is a tie-break.
   */
  @Test
  fun `the highest version wins regardless of the order returned`() {
    val candidates = listOf(release(12), release(15), release(13))

    val status = UpdateChannel.pickUpdate(candidates, installedVersionCode = 14)

    assertEquals(UpdateStatus.Available(release(15)), status)
  }

  @Test
  fun `an installed build at or above the newest release is up to date`() {
    val candidates = listOf(release(12), release(14))

    assertEquals(UpdateStatus.UpToDate, UpdateChannel.pickUpdate(candidates, 14))
    assertEquals(UpdateStatus.UpToDate, UpdateChannel.pickUpdate(candidates, 20))
  }

  @Test
  fun `no candidate at all is reported as a missing asset, not as up to date`() {
    assertEquals(
      UpdateStatus.Failed(UpdateFailure.NO_ASSET_FOR_CHANNEL),
      UpdateChannel.pickUpdate(emptyList(), installedVersionCode = 14),
    )
  }

  @Test
  fun `sizes are readable`() {
    assertEquals("unknown size", UpdateChannel.formatBytes(0))
    assertEquals("unknown size", UpdateChannel.formatBytes(-1))
    assertEquals("512 B", UpdateChannel.formatBytes(512))
    assertEquals("2.0 KB", UpdateChannel.formatBytes(2048))
    // The real v1.0.14 debug asset.
    assertEquals("24.4 MB", UpdateChannel.formatBytes(25_561_597))
    assertEquals("1.5 GB", UpdateChannel.formatBytes(1_610_612_736))
  }
}
