package com.example.ui.screens.auth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ProfileSetupScreen(viewModel: AuthViewModel, onProfileComplete: () -> Unit) {
  val state by viewModel.profile.collectAsState()

  LaunchedEffect(Unit) { viewModel.loadProfileFromSession() }

  LaunchedEffect(state.saved) {
    if (state.saved) onProfileComplete()
  }

  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
  ) {
    Spacer(modifier = Modifier.height(24.dp))
    Text("Profile Setup", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      "This helps helpers nearby know who they are working with.",
      fontSize = 16.sp,
      color = FixTheme.colors.textSecondary,
    )
    Spacer(modifier = Modifier.height(32.dp))

    AuthTextField(
      value = state.fullName,
      onValueChange = { viewModel.onProfileFieldChange(fullName = it) },
      label = "Full Name",
      leadingIcon = Icons.Default.Person,
      error = state.fullNameError?.message("Full name"),
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Next,
      onImeAction = {},
      enabled = !state.isSubmitting,
    )
    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Box(modifier = Modifier.weight(1f)) {
        AuthTextField(
          value = state.gender,
          onValueChange = { viewModel.onProfileFieldChange(gender = it) },
          label = "Gender (Optional)",
          leadingIcon = Icons.Default.Wc,
          error = null,
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.Next,
          onImeAction = {},
          enabled = !state.isSubmitting,
        )
      }
      Box(modifier = Modifier.weight(1f)) {
        AuthTextField(
          value = state.dateOfBirth,
          onValueChange = { viewModel.onProfileFieldChange(dateOfBirth = it) },
          label = "Birth Year (Optional)",
          leadingIcon = Icons.Default.Cake,
          error = null,
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
          onImeAction = {},
          enabled = !state.isSubmitting,
        )
      }
    }
    Spacer(modifier = Modifier.height(16.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Box(modifier = Modifier.weight(1f)) {
        AuthTextField(
          value = state.city,
          onValueChange = { viewModel.onProfileFieldChange(city = it) },
          label = "City",
          leadingIcon = Icons.Default.LocationCity,
          error = null,
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.Next,
          onImeAction = {},
          enabled = !state.isSubmitting,
        )
      }
      Box(modifier = Modifier.weight(1f)) {
        AuthTextField(
          value = state.state,
          onValueChange = { viewModel.onProfileFieldChange(state = it) },
          label = "State",
          leadingIcon = Icons.Default.Map,
          error = null,
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.Next,
          onImeAction = {},
          enabled = !state.isSubmitting,
        )
      }
    }
    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.pinCode,
      onValueChange = { viewModel.onProfileFieldChange(pinCode = it) },
      label = "PIN Code (Optional)",
      leadingIcon = Icons.Default.Pin,
      error = state.pinCodeError?.message("PIN code"),
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Done,
      onImeAction = { viewModel.submitProfile() },
      enabled = !state.isSubmitting,
    )

    Spacer(modifier = Modifier.height(32.dp))
    SubmitButton(
      text = "Continue",
      isSubmitting = state.isSubmitting,
      onClick = { viewModel.submitProfile() },
    )
    Spacer(modifier = Modifier.height(24.dp))
  }
}

/**
 * Asks for the permissions the app genuinely uses, through the real system dialogs.
 *
 * This screen used to describe permissions and then request nothing at all, so "Allow" and "Skip"
 * did exactly the same thing and location never worked.
 */
@Composable
fun PermissionRequestScreen(onPermissionsHandled: () -> Unit) {
  val context = LocalContext.current
  var locationGranted by remember { mutableStateOf(context.hasLocationPermission()) }
  var notificationsGranted by remember { mutableStateOf(context.hasNotificationPermission()) }
  var deniedPermanently by remember { mutableStateOf(false) }

  val launcher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
      results ->
      locationGranted = context.hasLocationPermission()
      notificationsGranted = context.hasNotificationPermission()
      // Android reports an immediate denial without showing a dialog once the user has chosen
      // "Don't ask again", which is the only case where Settings is the honest next step.
      deniedPermanently = results.isNotEmpty() && results.values.none { it } && !locationGranted
    }

  val allHandled = locationGranted && notificationsGranted

  Column(
    modifier = Modifier.fillMaxSize().padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(modifier = Modifier.height(48.dp))

    PermissionRow(
      icon = Icons.Default.LocationOn,
      iconTint = FixTheme.colors.primary,
      background = LightBlueBorder,
      title = "Location",
      description = "Used to show tasks and helpers near you, and to fill in your task address.",
      granted = locationGranted,
    )

    Spacer(modifier = Modifier.height(32.dp))

    PermissionRow(
      icon = Icons.Default.NotificationsActive,
      iconTint = FixTheme.colors.accentGraphic,
      background = LightOrangeBorder,
      title = "Notifications",
      description = "Used to tell you when someone accepts your task or sends a message.",
      granted = notificationsGranted,
    )

    if (deniedPermanently) {
      Spacer(modifier = Modifier.height(24.dp))
      FormErrorBanner(
        "Permissions were turned off for this app. You can enable them in Settings, or continue without them."
      )
    }

    Spacer(modifier = Modifier.weight(1f))

    if (allHandled) {
      SubmitButton(text = "Continue", isSubmitting = false, onClick = onPermissionsHandled)
    } else if (deniedPermanently) {
      SubmitButton(
        text = "Open Settings",
        isSubmitting = false,
        onClick = { context.openAppSettings() },
      )
    } else {
      SubmitButton(
        text = "Allow Permissions",
        isSubmitting = false,
        onClick = { launcher.launch(requiredPermissions()) },
      )
    }

    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = onPermissionsHandled) {
      Text(
        if (allHandled) "Skip" else "Continue without these",
        color = FixTheme.colors.textSecondary,
        fontWeight = FontWeight.Medium,
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      "You can change this later in your device settings.",
      fontSize = 12.sp,
      color = FixTheme.colors.textSecondary,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun PermissionRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: androidx.compose.ui.graphics.Color,
  background: androidx.compose.ui.graphics.Color,
  title: String,
  description: String,
  granted: Boolean,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier.size(72.dp).clip(CircleShape).background(background),
      contentAlignment = Alignment.Center,
    ) {
      Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(36.dp))
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary)
      if (granted) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = "Granted",
          tint = FixTheme.colors.success,
          modifier = Modifier.size(18.dp),
        )
      }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(description, fontSize = 14.sp, color = FixTheme.colors.textSecondary, textAlign = TextAlign.Center)
  }
}

private fun requiredPermissions(): Array<String> =
  buildList {
      add(Manifest.permission.ACCESS_COARSE_LOCATION)
      add(Manifest.permission.ACCESS_FINE_LOCATION)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
    .toTypedArray()

private fun Context.hasLocationPermission(): Boolean =
  checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
    checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

/** Below Android 13 notifications need no runtime grant, so there is nothing to ask for. */
private fun Context.hasNotificationPermission(): Boolean =
  Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
    checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)

private fun checkSelfPermission(context: Context, permission: String): Boolean =
  context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
  startActivity(
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  )
}
