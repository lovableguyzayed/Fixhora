package com.example.ui.screens.helper

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.FixhoraApplication
import com.example.R
import com.example.data.room.TaskEntity
import com.example.ui.theme.FixTheme
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

/** Where to look when no job can be placed: the middle of India, zoomed out to the whole country. */
private val DefaultCenter = GeoPoint(22.5, 79.0)
private const val DEFAULT_ZOOM = 4.5
private const val SINGLE_JOB_ZOOM = 14.0

/**
 * Addresses already looked up this session, so revisiting the tab or changing a filter does not
 * geocode the same street again. A miss is remembered too, as null, so a bad address is not
 * retried on every recomposition.
 */
private object GeocodeCache {
  val results = ConcurrentHashMap<String, Pair<Double, Double>>()
  val misses: MutableSet<String> = ConcurrentHashMap.newKeySet()
}

/** Where each job is, by task id. Jobs whose address cannot be found are simply absent. */
data class JobLocations(val points: Map<Int, GeoPoint>, val resolving: Boolean)

/**
 * Places each job on the map.
 *
 * Coordinates come from the task when the customer shared their location, and otherwise from the
 * device geocoder applied to the address they typed. Nothing is invented: a job whose address the
 * geocoder cannot find is left off the map and stays in the list below it.
 */
@Composable
fun rememberJobLocations(tasks: List<TaskEntity>): JobLocations {
  val application = LocalContext.current.applicationContext as? FixhoraApplication
  val key = tasks.map { Triple(it.id, it.locationQuery, it.latitude) }
  val state by
    produceState(initialValue = JobLocations(emptyMap(), resolving = true), key) {
      val points = mutableMapOf<Int, GeoPoint>()
      for (task in tasks) {
        val lat = task.latitude
        val lon = task.longitude
        if (lat != null && lon != null) {
          points[task.id] = GeoPoint(lat, lon)
          continue
        }
        val query = task.locationQuery.trim()
        if (query.isEmpty() || query in GeocodeCache.misses) continue
        val cached = GeocodeCache.results[query]
        val found =
          cached
            ?: application?.locationProvider?.searchAddresses(query)?.firstOrNull()?.let {
              (it.latitude to it.longitude).also { pair -> GeocodeCache.results[query] = pair }
            }
        if (found == null) GeocodeCache.misses += query
        else points[task.id] = GeoPoint(found.first, found.second)
        // Publish as we go, so pins appear one by one instead of all after the slowest lookup.
        value = JobLocations(points.toMap(), resolving = true)
      }
      value = JobLocations(points.toMap(), resolving = false)
    }
  return state
}

/**
 * An OpenStreetMap view with one pin per job.
 *
 * OpenStreetMap rather than Google Maps because it needs no API key — the app has none, which is
 * why this tab used to say the map was unavailable. Tiles are cached in the app's cache directory,
 * so nothing is written to shared storage and no storage permission is needed.
 */
@Composable
fun JobsMap(
  tasks: List<TaskEntity>,
  locations: JobLocations,
  isDark: Boolean,
  onJobSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = FixTheme.colors
  val lifecycleOwner = LocalLifecycleOwner.current
  val onSelected by rememberUpdatedState(onJobSelected)
  val mapDescription = stringResource(R.string.cd_jobs_map)
  // Created by AndroidView, with the context it is attached under; kept here for the lifecycle.
  val mapViewHolder = remember { arrayOfNulls<MapView>(1) }

  // MapView pauses its tile loading with the screen, and must be detached when it leaves, or it
  // keeps its download threads alive.
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> mapViewHolder[0]?.onResume()
        Lifecycle.Event.ON_PAUSE -> mapViewHolder[0]?.onPause()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      mapViewHolder[0]?.onPause()
      mapViewHolder[0]?.onDetach()
      mapViewHolder[0] = null
    }
  }

  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(Radius.lg))
        .background(colors.surfaceAlt)
        .semantics { contentDescription = mapDescription }
  ) {
    AndroidView(
      factory = { context -> createMapView(context).also { it.onResume(); mapViewHolder[0] = it } },
      modifier = Modifier.fillMaxSize(),
      update = { view ->
        // Map tiles are drawn light; inverted they sit comfortably in the dark theme.
        view.overlayManager.tilesOverlay.setColorFilter(if (isDark) TilesOverlay.INVERT_COLORS else null)
        view.overlays.removeAll { it is Marker }
        val placed = tasks.mapNotNull { task -> locations.points[task.id]?.let { task to it } }
        placed.forEach { (task, point) ->
          view.overlays +=
            Marker(view).apply {
              position = point
              title = task.descriptionTitle
              snippet = task.locationQuery
              setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
              setOnMarkerClickListener { marker, _ ->
                marker.showInfoWindow()
                onSelected(task.id)
                true
              }
            }
        }
        // Re-frame only when the set of pins changes, so a pan or zoom the user made is not
        // undone by an unrelated recomposition.
        val pins = placed.map { it.second }
        val framedKey = pins.map { it.latitude to it.longitude }
        if (view.tag != framedKey) {
          view.tag = framedKey
          frame(view, pins)
        }
        view.invalidate()
      },
    )

    // OpenStreetMap's licence requires the credit to be visible on the map itself.
    Text(
      text = stringResource(R.string.map_attribution),
      style = MaterialTheme.typography.labelSmall,
      color = colors.textSecondary,
      modifier =
        Modifier.align(Alignment.BottomEnd)
          .padding(Spacing.xs)
          .background(colors.surface.copy(alpha = 0.85f), RoundedCornerShape(Radius.sm))
          .padding(horizontal = Spacing.xs, vertical = 2.dp),
    )
  }
}

private fun createMapView(context: Context): MapView {
  Configuration.getInstance().apply {
    load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    // The tile servers refuse requests without an identifying user agent.
    userAgentValue = context.packageName
    osmdroidBasePath = File(context.cacheDir, "osmdroid")
    osmdroidTileCache = File(osmdroidBasePath, "tiles")
  }
  return MapView(context).apply {
    setTileSource(TileSourceFactory.MAPNIK)
    setMultiTouchControls(true)
    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
    isTilesScaledToDpi = true
    minZoomLevel = 3.0
    controller.setZoom(DEFAULT_ZOOM)
    controller.setCenter(DefaultCenter)
  }
}

/** Fits every pin on screen; one pin gets a street-level zoom, none leaves the country view. */
private fun frame(view: MapView, points: List<GeoPoint>) {
  when (points.size) {
    0 -> {
      view.controller.setZoom(DEFAULT_ZOOM)
      view.controller.setCenter(DefaultCenter)
    }
    1 -> {
      view.controller.setZoom(SINGLE_JOB_ZOOM)
      view.controller.setCenter(points.first())
    }
    else -> {
      val box = BoundingBox.fromGeoPointsSafe(points)
      // zoomToBoundingBox needs a measured view; before layout, post it to run after.
      if (view.width > 0 && view.height > 0) view.zoomToBoundingBox(box.increaseByScale(1.3f), false)
      else view.post { view.zoomToBoundingBox(box.increaseByScale(1.3f), false) }
    }
  }
}
