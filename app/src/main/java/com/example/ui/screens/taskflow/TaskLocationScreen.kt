package com.example.ui.screens.taskflow

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.BottomActionBar
import com.example.ui.components.FixButton
import com.example.ui.components.FixCard
import com.example.ui.components.StylizedMap
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing

import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TaskLocationScreen(onNext: () -> Unit, viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val fetchState by viewModel.locationFetchState.collectAsState()
    val suggestions by viewModel.addressSuggestions.collectAsState()
    val useCurrentLocation = uiState.useCurrentLocation
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        // Retry only if something was actually granted; otherwise the state already explains why.
        if (granted.values.any { it }) viewModel.useCurrentLocation()
    }

    fun requestCurrentLocation() {
        if (viewModel.hasLocationPermission()) {
            viewModel.useCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    var showError by remember { mutableStateOf(false) }
    val colors = FixTheme.colors

    // The draft starts with useCurrentLocation = true but no position, which showed the switch on
    // beside a caption saying it was off. The switch reflects what actually happened instead.
    val usingCurrentLocation = useCurrentLocation &&
        (fetchState == LocationFetchState.RESOLVING || uiState.latitude != null || uiState.locationQuery.isNotBlank())

    // A resolved fix without a readable address still locates the task, so coordinates alone
    // are enough to continue.
    val hasLocation = uiState.locationQuery.isNotBlank() || uiState.latitude != null

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
        ) {
            StepHeader(title = stringResource(R.string.loc_title), subtitle = stringResource(R.string.loc_subtitle))

            StepTextField(
                value = uiState.locationQuery,
                onValueChange = {
                    viewModel.updateLocationQuery(it)
                    showError = false
                },
                placeholder = stringResource(R.string.loc_search),
                leadingIcon = Icons.Default.LocationOn,
                trailingIcon = {
                    IconButton(onClick = { requestCurrentLocation() }) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = stringResource(R.string.loc_use_current),
                            tint = colors.primary
                        )
                    }
                }
            )

            // Real suggestions from the device geocoder. This used to be a hardcoded list of five
            // Indian addresses filtered by substring, presented as though it were address lookup.
            if (suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                FixCard {
                    Column {
                        suggestions.forEachIndexed { index, suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectSuggestion(suggestion) }
                                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.md))
                                Text(
                                    text = suggestion.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (index != suggestions.lastIndex) {
                                HorizontalDivider(color = colors.border, modifier = Modifier.padding(start = 46.dp))
                            }
                        }
                    }
                }
            }

            val locationMessage = fetchState.explain()
            if (locationMessage != null) {
                Spacer(modifier = Modifier.height(Spacing.md))
                LocationNotice(
                    message = locationMessage,
                    isError = fetchState != LocationFetchState.RESOLVING,
                    actionLabel = if (fetchState == LocationFetchState.SERVICES_DISABLED) stringResource(R.string.action_open_settings) else null,
                    onAction = {
                        context.startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            FixCard {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        StylizedMap(modifier = Modifier.matchParentSize())
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (usingCurrentLocation) colors.primary else colors.accentGraphic,
                            modifier = Modifier
                                .size(44.dp)
                                .offset(y = (-14).dp)
                        )

                        // This graphic is not a map and never was. Saying so stops it from reading
                        // as a real pin dropped at the user's address.
                        Surface(
                            color = colors.surface.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(Radius.sm),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(Spacing.sm)
                        ) {
                            Text(
                                stringResource(R.string.loc_map_preview),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                            )
                        }
                    }
                    HorizontalDivider(color = colors.border)
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (fetchState == LocationFetchState.RESOLVING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colors.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.loc_use_current),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.textPrimary
                            )
                            Text(
                                text = when {
                                    fetchState == LocationFetchState.RESOLVING -> stringResource(R.string.loc_finding_you)
                                    usingCurrentLocation && uiState.locationQuery.isNotBlank() -> uiState.locationQuery
                                    usingCurrentLocation && uiState.latitude != null ->
                                        stringResource(R.string.loc_position_no_address)
                                    else -> stringResource(R.string.loc_switch_off)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Switch(
                            checked = usingCurrentLocation,
                            enabled = fetchState != LocationFetchState.RESOLVING,
                            onCheckedChange = { enabled ->
                                showError = false
                                if (enabled) requestCurrentLocation()
                                else viewModel.stopUsingCurrentLocation()
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = colors.primary,
                                checkedThumbColor = colors.onPrimary,
                                uncheckedTrackColor = colors.surfaceAlt,
                                uncheckedBorderColor = colors.border,
                                uncheckedThumbColor = colors.textSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Service area as direct choices. It used to be a card that read a hardcoded
            // "Within 5 km" and did nothing; then a card with a chevron that opened a dialog of
            // radio buttons. One tap is enough.
            Text(
                text = stringResource(R.string.loc_service_area_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = stringResource(R.string.loc_service_area_body),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TaskViewModel.DISTANCE_OPTIONS_KM.forEach { km ->
                    val selected = uiState.selectedDistance == km
                    val shape = RoundedCornerShape(Radius.md)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(MinTouchTarget)
                            .clip(shape)
                            .selectable(selected = selected, role = Role.RadioButton) {
                                viewModel.updateSelectedDistance(km)
                            },
                        shape = shape,
                        color = if (selected) colors.primarySurface else colors.surface,
                        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.border)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                stringResource(R.string.loc_km_short, km),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selected) colors.primary else colors.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Surface(
                color = colors.primarySurface,
                shape = RoundedCornerShape(Radius.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(Spacing.lg), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.loc_safe_title), style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Text(stringResource(R.string.loc_safe_body), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }

        BottomActionBar {
            if (showError) {
                Text(
                    text = stringResource(R.string.loc_required),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
            }
            FixButton(
                text = stringResource(R.string.action_continue),
                onClick = {
                    if (hasLocation) {
                        viewModel.dismissAddressSuggestions()
                        onNext()
                    } else {
                        showError = true
                    }
                }
            )
        }
    }
}

/**
 * Says which of the four ordinary location failures happened, and what the user can do next.
 * Returns null when there is nothing to report.
 *
 * Composable because the wording comes from resources, which is what makes it follow the app's
 * language setting. Its one caller is already inside a composable.
 */
@Composable
private fun LocationFetchState.explain(): String? =
    when (this) {
        LocationFetchState.IDLE -> null
        LocationFetchState.RESOLVING -> stringResource(R.string.loc_finding)
        LocationFetchState.PERMISSION_REQUIRED ->
            stringResource(R.string.loc_error_permission)
        LocationFetchState.SERVICES_DISABLED ->
            stringResource(R.string.loc_error_disabled)
        LocationFetchState.UNAVAILABLE ->
            stringResource(R.string.loc_error_no_fix)
        LocationFetchState.NO_ADDRESS_FOUND ->
            stringResource(R.string.loc_error_no_address)
    }

@Composable
private fun LocationNotice(
    message: String,
    isError: Boolean,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Surface(
        color = if (isError) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            FixTheme.colors.primary.copy(alpha = 0.08f)
        },
        shape = RoundedCornerShape(Radius.md),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )
            if (actionLabel != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel, color = FixTheme.colors.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
