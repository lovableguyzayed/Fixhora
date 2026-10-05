package com.example.data.update

import com.example.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

/** How long a single call is allowed to hang before the check gives up and says so. */
private const val TIMEOUT_MS = 10_000

/**
 * Asks the GitHub releases API whether a newer build of *this channel* exists.
 *
 * Uses `HttpURLConnection` and the framework's `org.json` rather than a HTTP or JSON library:
 * Batch 0 removed Retrofit, OkHttp and Moshi because nothing used them, and one API call for one
 * small payload is not a reason to bring three dependencies back.
 *
 * [fetch] is injectable so the parsing and decision path can be tested without a network.
 */
class UpdateRepository(
  private val installedVersionCode: Int = BuildConfig.VERSION_CODE,
  private val debugChannel: Boolean = BuildConfig.DEBUG,
  private val fetch: (String) -> FetchResult = ::httpGet,
) {

  suspend fun check(): UpdateStatus =
    withContext(Dispatchers.IO) {
      when (val result = fetch(UpdateChannel.RELEASES_API)) {
        is FetchResult.Failure -> UpdateStatus.Failed(result.failure)
        is FetchResult.Body ->
          try {
            UpdateChannel.pickUpdate(parseReleases(result.text, debugChannel), installedVersionCode)
          } catch (_: JSONException) {
            UpdateStatus.Failed(UpdateFailure.MALFORMED_RESPONSE)
          }
      }
    }
}

/** The raw outcome of one HTTP call, before any interpretation. */
sealed interface FetchResult {
  data class Body(val text: String) : FetchResult

  data class Failure(val failure: UpdateFailure) : FetchResult
}

/**
 * Pulls every release that carries an APK this build is allowed to install.
 *
 * Skips rather than fails on anything unrecognised — a draft, a pre-release, a hand-made tag that
 * is not `v1.0.<code>`, or a release whose assets do not include this channel's APK. One odd entry
 * in the list must not hide a real update sitting behind it.
 *
 * Throws [JSONException] only when the payload is not a JSON array at all, which means the caller
 * is talking to something other than the releases API.
 */
internal fun parseReleases(body: String, debugChannel: Boolean): List<ReleaseInfo> {
  val releases = JSONArray(body)
  val candidates = mutableListOf<ReleaseInfo>()

  for (i in 0 until releases.length()) {
    val release = releases.optJSONObject(i) ?: continue
    if (release.optBoolean("draft", false)) continue
    if (release.optBoolean("prerelease", false)) continue

    val tag = release.optString("tag_name")
    val versionCode = UpdateChannel.versionCodeFromTag(tag) ?: continue
    val versionName = UpdateChannel.versionNameFromTag(tag)
    val wantedAsset = UpdateChannel.apkAssetName(versionName, debugChannel)

    val assets = release.optJSONArray("assets") ?: continue
    for (j in 0 until assets.length()) {
      val asset = assets.optJSONObject(j) ?: continue
      if (asset.optString("name") != wantedAsset) continue
      // An asset still uploading has a URL that 404s, which would look like a broken update.
      if (asset.optString("state") != "uploaded") continue
      val url = asset.optString("browser_download_url")
      if (url.isEmpty()) continue

      candidates += ReleaseInfo(versionCode, versionName, url, asset.optLong("size", 0L))
      break
    }
  }

  return candidates
}

/**
 * One plain GET.
 *
 * Unauthenticated: the repository is public, so no token is needed — and shipping one inside an
 * APK would hand it to anyone who unzips the file.
 */
internal fun httpGet(url: String): FetchResult {
  var connection: HttpURLConnection? = null
  return try {
    connection =
      (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = TIMEOUT_MS
        readTimeout = TIMEOUT_MS
        setRequestProperty("Accept", "application/vnd.github+json")
        setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        // GitHub rejects requests with no User-Agent outright.
        setRequestProperty("User-Agent", "Fixhora-Android")
      }

    when (connection.responseCode) {
      in 200..299 ->
        FetchResult.Body(connection.inputStream.bufferedReader().use { it.readText() })
      // Unauthenticated calls get 60 an hour; 403 with the limit exhausted and 429 both mean wait.
      HttpURLConnection.HTTP_FORBIDDEN,
      429 -> FetchResult.Failure(UpdateFailure.RATE_LIMITED)
      else -> {
        // Read and discard so the connection can be pooled rather than torn down.
        connection.errorStream?.use { it.readBytes() }
        FetchResult.Failure(UpdateFailure.SERVER_ERROR)
      }
    }
  } catch (_: IOException) {
    FetchResult.Failure(UpdateFailure.NO_NETWORK)
  } finally {
    connection?.disconnect()
  }
}
