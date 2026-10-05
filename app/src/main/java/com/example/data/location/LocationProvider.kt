package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/** A place the user can pick, as returned by address search. */
data class AddressSuggestion(
  val label: String,
  val latitude: Double,
  val longitude: Double,
)

/** Outcome of asking for the device's position. */
sealed interface LocationOutcome {
  data class Resolved(val latitude: Double, val longitude: Double, val address: String?) :
    LocationOutcome

  /** The user has not granted location access. */
  data object PermissionMissing : LocationOutcome

  /** Location services are switched off device-wide. */
  data object LocationDisabled : LocationOutcome

  /** Permission and services are fine, but no fix arrived in time. */
  data object Unavailable : LocationOutcome
}

/**
 * Reads the device's real position and turns coordinates into addresses.
 *
 * Every path has a defined failure result rather than an exception, because on a phone all four
 * outcomes above are ordinary: permission refused, GPS switched off, indoors with no fix, or no
 * geocoder backend installed. The caller needs to tell the user which one happened.
 */
class LocationProvider(context: Context) {

  private val appContext = context.applicationContext
  private val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)

  fun hasPermission(): Boolean =
    appContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED ||
      appContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

  private fun isLocationEnabled(): Boolean {
    val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    return manager?.let {
      it.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } ?: false
  }

  suspend fun currentLocation(): LocationOutcome {
    if (!hasPermission()) return LocationOutcome.PermissionMissing
    if (!isLocationEnabled()) return LocationOutcome.LocationDisabled

    val location =
      withTimeoutOrNull(FIX_TIMEOUT_MS) { requestFix() ?: lastKnownLocation() }
        ?: return LocationOutcome.Unavailable

    return LocationOutcome.Resolved(
      latitude = location.latitude,
      longitude = location.longitude,
      address = reverseGeocode(location.latitude, location.longitude),
    )
  }

  /**
   * Balanced accuracy rather than high: placing a task in the right neighbourhood does not need a
   * GPS-grade fix, and asking for one drains the battery and takes far longer indoors.
   */
  @SuppressLint("MissingPermission") // guarded by hasPermission() above
  private suspend fun requestFix(): Location? = suspendCancellableCoroutine { continuation ->
    val cancellationSource = CancellationTokenSource()
    fusedClient
      .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationSource.token)
      .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
      .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
    continuation.invokeOnCancellation { cancellationSource.cancel() }
  }

  @SuppressLint("MissingPermission") // guarded by hasPermission() above
  private suspend fun lastKnownLocation(): Location? = suspendCancellableCoroutine { continuation ->
    fusedClient.lastLocation
      .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
      .addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
  }

  /** Null when the device has no geocoder backend or the lookup fails; the caller falls back. */
  suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
    withContext(Dispatchers.IO) {
      if (!Geocoder.isPresent()) return@withContext null
      try {
        @Suppress("DEPRECATION")
        Geocoder(appContext, Locale.getDefault())
          .getFromLocation(latitude, longitude, 1)
          ?.firstOrNull()
          ?.toDisplayLine()
      } catch (e: IOException) {
        null
      } catch (e: IllegalArgumentException) {
        null
      }
    }

  /**
   * Real address search, replacing the hardcoded list of five Indian addresses that used to be
   * presented as autocomplete. Returns an empty list when the device cannot geocode, so nothing
   * fabricated is ever shown.
   */
  suspend fun searchAddresses(query: String): List<AddressSuggestion> =
    withContext(Dispatchers.IO) {
      if (query.isBlank() || query.length < MIN_SEARCH_LENGTH) return@withContext emptyList()
      if (!Geocoder.isPresent()) return@withContext emptyList()
      try {
        @Suppress("DEPRECATION")
        Geocoder(appContext, Locale.getDefault())
          .getFromLocationName(query, MAX_SUGGESTIONS)
          ?.mapNotNull { address ->
            val label = address.toDisplayLine() ?: return@mapNotNull null
            AddressSuggestion(label, address.latitude, address.longitude)
          }
          .orEmpty()
      } catch (e: IOException) {
        emptyList()
      } catch (e: IllegalArgumentException) {
        emptyList()
      }
    }

  private fun Address.toDisplayLine(): String? {
    getAddressLine(0)?.takeIf { it.isNotBlank() }?.let {
      return it
    }
    val parts = listOfNotNull(subLocality, locality, adminArea, postalCode).filter { it.isNotBlank() }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(", ")
  }

  private companion object {
    const val FIX_TIMEOUT_MS = 12_000L
    const val MAX_SUGGESTIONS = 5
    const val MIN_SEARCH_LENGTH = 3
  }
}
