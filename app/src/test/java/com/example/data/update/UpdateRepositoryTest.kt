package com.example.data.update

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Exercises the parsing and decision path against payloads shaped like the real releases API.
 *
 * The HTTP call itself is swapped out: what matters here is that a real response produces the
 * right answer, and that a malformed or unhelpful one produces a specific failure rather than a
 * silent "no update".
 */
class UpdateRepositoryTest {

  private fun releaseJson(
    tag: String,
    assetName: String,
    draft: Boolean = false,
    prerelease: Boolean = false,
    state: String = "uploaded",
    size: Long = 25_561_597L,
  ) =
    """
    {
      "tag_name": "$tag",
      "name": "Fixhora ${tag.removePrefix("v")}",
      "draft": $draft,
      "prerelease": $prerelease,
      "assets": [
        {
          "name": "$assetName",
          "state": "$state",
          "size": $size,
          "browser_download_url": "https://github.com/lovableguyzayed/Fixhora/releases/download/$tag/$assetName"
        }
      ]
    }
    """
      .trimIndent()

  private fun repository(
    body: String,
    debugChannel: Boolean = true,
    installedVersionCode: Int = 14,
  ) =
    UpdateRepository(
      installedVersionCode = installedVersionCode,
      debugChannel = debugChannel,
      fetch = { FetchResult.Body(body) },
    )

  @Test
  fun `a newer release for this channel is offered`() = runTest {
    val body = "[${releaseJson("v1.0.15", "fixhora-1.0.15-debug.apk")}]"

    val status = repository(body).check()

    assertEquals(
      UpdateStatus.Available(
        ReleaseInfo(
          versionCode = 15,
          versionName = "1.0.15",
          apkUrl =
            "https://github.com/lovableguyzayed/Fixhora/releases/download/v1.0.15/fixhora-1.0.15-debug.apk",
          sizeBytes = 25_561_597L,
        )
      ),
      status,
    )
  }

  /**
   * The two channels have different application ids and different signing keys, so installing the
   * other channel's APK fails outright. A release build must never be offered the debug asset.
   */
  @Test
  fun `a release build ignores the debug asset`() = runTest {
    val body = "[${releaseJson("v1.0.15", "fixhora-1.0.15-debug.apk")}]"

    val status = repository(body, debugChannel = false).check()

    assertEquals(UpdateStatus.Failed(UpdateFailure.NO_ASSET_FOR_CHANNEL), status)
  }

  @Test
  fun `a debug build ignores the release asset`() = runTest {
    val body = "[${releaseJson("v1.0.15", "fixhora-1.0.15.apk")}]"

    val status = repository(body, debugChannel = true).check()

    assertEquals(UpdateStatus.Failed(UpdateFailure.NO_ASSET_FOR_CHANNEL), status)
  }

  @Test
  fun `the highest version wins, whatever order the API returns`() = runTest {
    val body =
      listOf(
          releaseJson("v1.0.13", "fixhora-1.0.13-debug.apk"),
          releaseJson("v1.0.16", "fixhora-1.0.16-debug.apk"),
          releaseJson("v1.0.15", "fixhora-1.0.15-debug.apk"),
        )
        .joinToString(prefix = "[", separator = ",", postfix = "]")

    val status = repository(body).check()

    assertEquals(16, (status as UpdateStatus.Available).release.versionCode)
  }

  @Test
  fun `drafts and pre-releases are skipped so an unfinished build is never offered`() = runTest {
    val body =
      listOf(
          releaseJson("v1.0.20", "fixhora-1.0.20-debug.apk", draft = true),
          releaseJson("v1.0.19", "fixhora-1.0.19-debug.apk", prerelease = true),
          releaseJson("v1.0.15", "fixhora-1.0.15-debug.apk"),
        )
        .joinToString(prefix = "[", separator = ",", postfix = "]")

    val status = repository(body).check()

    assertEquals(15, (status as UpdateStatus.Available).release.versionCode)
  }

  /** An asset still uploading has a URL that 404s, which would look like a broken update. */
  @Test
  fun `an asset that has not finished uploading is skipped`() = runTest {
    val body =
      listOf(
          releaseJson("v1.0.16", "fixhora-1.0.16-debug.apk", state = "starter"),
          releaseJson("v1.0.15", "fixhora-1.0.15-debug.apk"),
        )
        .joinToString(prefix = "[", separator = ",", postfix = "]")

    val status = repository(body).check()

    assertEquals(15, (status as UpdateStatus.Available).release.versionCode)
  }

  @Test
  fun `a hand made tag that is not the CI shape is skipped, not treated as version zero`() =
    runTest {
      val body =
        listOf(
            releaseJson("nightly", "fixhora-nightly-debug.apk"),
            releaseJson("v1.0.15", "fixhora-1.0.15-debug.apk"),
          )
          .joinToString(prefix = "[", separator = ",", postfix = "]")

      val status = repository(body).check()

      assertEquals(15, (status as UpdateStatus.Available).release.versionCode)
    }

  @Test
  fun `the installed build being the newest is up to date`() = runTest {
    val body = "[${releaseJson("v1.0.14", "fixhora-1.0.14-debug.apk")}]"

    assertEquals(UpdateStatus.UpToDate, repository(body, installedVersionCode = 14).check())
  }

  /** An older release must never be offered: Android refuses to install a lower versionCode. */
  @Test
  fun `an older release is not offered as an update`() = runTest {
    val body = "[${releaseJson("v1.0.12", "fixhora-1.0.12-debug.apk")}]"

    assertEquals(UpdateStatus.UpToDate, repository(body, installedVersionCode = 14).check())
  }

  @Test
  fun `an empty release list reports a missing asset`() = runTest {
    assertEquals(
      UpdateStatus.Failed(UpdateFailure.NO_ASSET_FOR_CHANNEL),
      repository("[]").check(),
    )
  }

  @Test
  fun `a response that is not the releases list is reported, not swallowed`() = runTest {
    assertEquals(
      UpdateStatus.Failed(UpdateFailure.MALFORMED_RESPONSE),
      repository("""{"message":"Not Found"}""").check(),
    )
    assertEquals(
      UpdateStatus.Failed(UpdateFailure.MALFORMED_RESPONSE),
      repository("<html>proxy error</html>").check(),
    )
  }

  @Test
  fun `a transport failure keeps its own reason`() = runTest {
    val repository =
      UpdateRepository(
        installedVersionCode = 14,
        debugChannel = true,
        fetch = { FetchResult.Failure(UpdateFailure.RATE_LIMITED) },
      )

    assertEquals(UpdateStatus.Failed(UpdateFailure.RATE_LIMITED), repository.check())
  }
}
