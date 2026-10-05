package com.example.ui.screens.taskflow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.BottomActionBar
import com.example.ui.components.ErrorText
import com.example.ui.components.FixhoraCard
import com.example.ui.components.FixhoraTextField
import com.example.ui.components.InfoCard
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SectionTitle
import com.example.ui.components.Spacing
import com.example.ui.components.StepHeader
import com.example.ui.components.StylizedMap
import com.example.ui.components.UserLocationDot
import com.example.ui.theme.*

/** Address used while "Use my current location" is on (no GPS lookup is wired up yet). */
const val CurrentLocationAddress = "Sector 62, Noida, Uttar Pradesh 201309"

private val mockSuggestions = listOf(
    "Sector 62, Noida, Uttar Pradesh 201309",
    "Connaught Place, New Delhi, Delhi 110001",
    "Indiranagar, Bengaluru, Karnataka 560038",
    "Bandra West, Mumbai, Maharashtra 400050",
    "Salt Lake, Kolkata, West Bengal 700091"
)

private val distanceOptions = listOf(2, 5, 10, 25)

@Composable
fun TaskLocationScreen(onNext: () -> Unit, viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val useCurrentLocation = uiState.useCurrentLocation
    var showError by remember { mutableStateOf(false) }

    val filteredSuggestions = if (uiState.locationQuery.isNotBlank() && !useCurrentLocation) {
        mockSuggestions.filter { it.contains(uiState.locationQuery, ignoreCase = true) && it != uiState.locationQuery }
    } else {
        emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen)
        ) {
            StepHeader(title = "Where do you need help?", subtitle = "Set the location for your task")

            FixhoraTextField(
                value = uiState.locationQuery,
                onValueChange = {
                    viewModel.updateLocationQuery(it)
                    if (it.isNotEmpty()) viewModel.updateUseCurrentLocation(false)
                    showError = false
                },
                placeholder = "Search address or area",
                leadingIcon = Icons.Default.LocationOn,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.updateLocationQuery("")
                        viewModel.updateUseCurrentLocation(true)
                        showError = false
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Use current location", tint = BluePrimary, modifier = Modifier.size(20.dp))
                    }
                }
            )

            // Address autocomplete suggestions
            if (filteredSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xxs))
                FixhoraCard(modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                    filteredSuggestions.forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateLocationQuery(suggestion)
                                    viewModel.updateUseCurrentLocation(false)
                                }
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = HintGrey, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (index != filteredSuggestions.lastIndex) {
                            HorizontalDivider(color = DividerGrey, modifier = Modifier.padding(start = 46.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Map preview + current location toggle
            FixhoraCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StylizedMap(modifier = Modifier.matchParentSize())
                    if (useCurrentLocation) {
                        UserLocationDot()
                    } else {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = OrangeSecondary,
                            modifier = Modifier
                                .size(44.dp)
                                .offset(y = (-14).dp)
                        )
                    }
                }
                HorizontalDivider(color = BorderGrey)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateUseCurrentLocation(!useCurrentLocation) }
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Use my current location", style = MaterialTheme.typography.titleSmall, color = DarkNavy)
                        Text(
                            text = if (useCurrentLocation) CurrentLocationAddress else uiState.locationQuery.ifBlank { "Turn on to use where you are now" },
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Switch(
                        checked = useCurrentLocation,
                        onCheckedChange = {
                            viewModel.updateUseCurrentLocation(it)
                            showError = false
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = BluePrimary,
                            uncheckedTrackColor = MutedSurface,
                            uncheckedBorderColor = BorderGrey,
                            uncheckedThumbColor = HintGrey
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            SectionTitle("Service area")
            Spacer(modifier = Modifier.height(Spacing.xxs))
            Text(
                text = "You'll receive offers from helpers within ${uiState.selectedDistance} km.",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryGrey
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                distanceOptions.forEach { km ->
                    val selected = uiState.selectedDistance == km
                    Surface(
                        onClick = { viewModel.updateSelectedDistance(km) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        color = if (selected) BlueContainer else Color.White,
                        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) BluePrimary else BorderGrey)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "$km km",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selected) BluePrimary else DarkNavy
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            InfoCard(
                icon = Icons.Default.Shield,
                title = "Your location is safe with us",
                message = "Helpers only see your approximate area until you accept an offer."
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        BottomActionBar {
            if (showError) {
                ErrorText(
                    "Enter an address or turn on \"Use my current location\"",
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
            }
            PrimaryButton(
                text = "Continue",
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = {
                    if (useCurrentLocation || uiState.locationQuery.isNotBlank()) onNext() else showError = true
                }
            )
        }
    }
}
