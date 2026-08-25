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
import com.example.ui.theme.FixTheme

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.loc_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        
        Text(
            text = stringResource(R.string.loc_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
        )
        
        var showError by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = uiState.locationQuery,
            onValueChange = { viewModel.updateLocationQuery(it) },
            placeholder = { Text(stringResource(R.string.loc_search)) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                IconButton(onClick = { requestCurrentLocation() }) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.loc_use_current),
                        tint = FixTheme.colors.primary
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        )

        // Real suggestions from the device geocoder. This used to be a hardcoded list of five
        // Indian addresses filtered by substring, presented as though it were address lookup.
        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column {
                    suggestions.forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSuggestion(suggestion) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = suggestion.label,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (index != suggestions.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }

        val locationMessage = fetchState.explain()
        if (locationMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
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

        Spacer(modifier = Modifier.height(16.dp))
        
        // Mock Map Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).background(FixTheme.colors.primarySurface),
                    contentAlignment = Alignment.Center
                ) {
                    // Map Pin
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(FixTheme.colors.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(FixTheme.colors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(FixTheme.colors.surface)
                            )
                        }
                    }

                    // This graphic is not a map and never was. Saying so stops it from reading as
                    // a real pin dropped at the user's address.
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.loc_map_preview),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (fetchState == LocationFetchState.RESOLVING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).padding(end = 4.dp),
                                color = FixTheme.colors.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = FixTheme.colors.primary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.loc_use_current), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(
                                text = when {
                                    fetchState == LocationFetchState.RESOLVING -> stringResource(R.string.loc_finding_you)
                                    useCurrentLocation && uiState.locationQuery.isNotBlank() ->
                                        uiState.locationQuery
                                    useCurrentLocation && uiState.latitude != null ->
                                        stringResource(R.string.loc_position_no_address)
                                    else -> stringResource(R.string.loc_switch_off)
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useCurrentLocation,
                            enabled = fetchState != LocationFetchState.RESOLVING,
                            onCheckedChange = { enabled ->
                                if (enabled) requestCurrentLocation()
                                else viewModel.stopUsingCurrentLocation()
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = FixTheme.colors.primary)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.loc_service_area_title),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // This card used to read a hardcoded "Within 5 km" and do nothing when tapped, while
        // selectedDistance was never set from anywhere.
        var showDistancePicker by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDistancePicker = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = FixTheme.colors.primary,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.loc_within_km, uiState.selectedDistance),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        stringResource(R.string.loc_tap_to_change),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.cd_change_service_area),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showDistancePicker) {
            AlertDialog(
                onDismissRequest = { showDistancePicker = false },
                title = { Text(stringResource(R.string.loc_service_area)) },
                text = {
                    Column {
                        Text(
                            stringResource(R.string.loc_service_area_body),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TaskViewModel.DISTANCE_OPTIONS_KM.forEach { km ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateSelectedDistance(km)
                                        showDistancePicker = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.selectedDistance == km,
                                    onClick = {
                                        viewModel.updateSelectedDistance(km)
                                        showDistancePicker = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = FixTheme.colors.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.loc_within_km, km), fontSize = 16.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDistancePicker = false }) {
                        Text(stringResource(R.string.action_done), color = FixTheme.colors.primary)
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = FixTheme.colors.primary.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, FixTheme.colors.primary.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = FixTheme.colors.primary,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.loc_safe_title), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(stringResource(R.string.loc_safe_body), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        // A resolved fix without a readable address still locates the task, so coordinates alone
        // are enough to continue.
        val hasLocation = uiState.locationQuery.isNotBlank() || uiState.latitude != null

        if (showError) {
            Text(
                text = stringResource(R.string.loc_required),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (hasLocation) {
                    viewModel.dismissAddressSuggestions()
                    onNext()
                } else {
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FixTheme.colors.primary)
        ) {
            Text(
                text = stringResource(R.string.action_continue),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
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
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                fontSize = 13.sp,
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
