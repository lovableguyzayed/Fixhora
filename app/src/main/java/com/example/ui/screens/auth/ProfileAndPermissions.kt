package com.example.ui.screens.auth

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.components.FixhoraCard
import com.example.ui.components.FixhoraTextField
import com.example.ui.components.IconBadge
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(onProfileComplete: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dobMillis by remember { mutableStateOf<Long?>(null) }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("English") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) photoUri = uri.toString()
    }
    val pickPhoto = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    val nameError = if (submitted && fullName.isBlank()) "Enter your full name" else null
    val pinError = if (submitted && pinCode.isNotEmpty() && pinCode.length != 6) "PIN code must be 6 digits" else null

    AuthScreenLayout(
        onBack = null,
        footer = {
            PrimaryButton(
                text = "Continue",
                onClick = {
                    submitted = true
                    if (fullName.isNotBlank() && (pinCode.isEmpty() || pinCode.length == 6)) onProfileComplete()
                }
            )
        }
    ) {
        ScreenHeader(title = "Set up your profile", subtitle = "Help people nearby get to know you.")
        Spacer(modifier = Modifier.height(Spacing.lg))

        // Avatar picker
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(104.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MutedSurface)
                        .border(1.dp, BorderGrey, CircleShape)
                        .clickable(onClick = pickPhoto),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = HintGrey, modifier = Modifier.size(48.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(BluePrimary)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable(onClick = pickPhoto),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Choose profile photo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            TextButton(onClick = pickPhoto) {
                Text(
                    if (photoUri == null) "Add profile photo (optional)" else "Change photo",
                    style = MaterialTheme.typography.titleSmall,
                    color = BluePrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        FixhoraTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = "Full name",
            leadingIcon = Icons.Default.Person,
            errorText = nameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(Spacing.sm))
        FieldLabel("Gender")
        ChoiceChipsRow(options = listOf("Male", "Female", "Other"), selected = gender, onSelect = { gender = it })

        Spacer(modifier = Modifier.height(Spacing.md))
        Box {
            FixhoraTextField(
                value = dobMillis?.let { formatDate(it) } ?: "",
                onValueChange = {},
                label = "Date of birth",
                placeholder = "DD MMM YYYY",
                leadingIcon = Icons.Default.CalendarMonth,
                readOnly = true
            )
            // Read-only fields swallow taps, so an overlay opens the picker instead.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShapeMedium)
                    .clickable { showDatePicker = true }
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FixhoraTextField(
                value = city,
                onValueChange = { city = it },
                label = "City",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            FixhoraTextField(
                value = state,
                onValueChange = { state = it },
                label = "State",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        FixhoraTextField(
            value = pinCode,
            onValueChange = { pinCode = it.digitsOnly(6) },
            label = "PIN code",
            errorText = pinError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )

        Spacer(modifier = Modifier.height(Spacing.sm))
        FieldLabel("Preferred language")
        ChoiceChipsRow(options = listOf("English", "हिंदी"), selected = language, onSelect = { language = it })
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dobMillis,
            yearRange = 1940..2012
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dobMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = SecondaryGrey) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun PermissionRequestScreen(onPermissionsHandled: () -> Unit) {
    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()
    // Whatever the user decides, onboarding continues; features degrade gracefully.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        onPermissionsHandled()
    }

    AuthScreenLayout(
        onBack = null,
        horizontalAlignment = Alignment.CenterHorizontally,
        footer = {
            PrimaryButton(text = "Allow Permissions", onClick = { launcher.launch(permissions) })
            Spacer(modifier = Modifier.height(Spacing.xxs))
            TextButton(onClick = onPermissionsHandled, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Not now", style = MaterialTheme.typography.labelLarge, color = SecondaryGrey)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(BlueContainer),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(Spacing.lg))
        ScreenHeader(
            title = "A couple of permissions",
            subtitle = "FixoraX works best with these turned on. You can change them anytime in Settings.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.xl))

        PermissionCard(
            icon = Icons.Default.LocationOn,
            tint = BluePrimary,
            title = "Location",
            message = "Show tasks and helpers near you and fill in your address faster."
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        PermissionCard(
            icon = Icons.Default.NotificationsActive,
            tint = OrangeSecondary,
            title = "Notifications",
            message = "Get instant updates about your tasks, messages and offers."
        )
    }
}

@Composable
private fun PermissionCard(icon: ImageVector, tint: Color, title: String, message: String) {
    FixhoraCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, tint = tint, size = 44.dp, iconSize = 24.dp)
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = DarkNavy)
                Text(message, style = MaterialTheme.typography.bodySmall, color = SecondaryGrey)
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = DarkNavy,
        modifier = Modifier.padding(bottom = Spacing.xs)
    )
}

/** Single-select pills that share the row width equally. */
@Composable
fun ChoiceChipsRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                onClick = { onSelect(option) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShapeMedium,
                color = if (isSelected) BlueContainer else Color.White,
                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) BluePrimary else BorderGrey)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isSelected) BluePrimary else DarkNavy
                    )
                }
            }
        }
    }
}

private val RoundedCornerShapeMedium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)

private fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(millis))
