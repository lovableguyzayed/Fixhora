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
import androidx.compose.ui.res.stringResource
import com.example.R
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
  // Localized by LocalizedContent, so validation messages honour the chosen language.
  val context = LocalContext.current

  LaunchedEffect(Unit) { viewModel.loadProfileFromSession() }

  LaunchedEffect(state.saved) {
    if (state.saved) onProfileComplete()
  }

  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
  ) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(stringResource(R.string.profile_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      stringResource(R.string.profile_subtitle),
      fontSize = 16.sp,
      color = FixTheme.colors.textSecondary,
    )
    Spacer(modifier = Modifier.height(32.dp))

    AuthTextField(
      value = state.fullName,
      onValueChange = { viewModel.onProfileFieldChange(fullName = it) },
      label = stringResource(R.string.field_full_name),
      leadingIcon = Icons.Default.Person,
      error = state.fullNameError?.message(context, R.string.label_full_name),
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
          label = stringResource(R.string.field_gender_optional),
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
          label = stringResource(R.string.field_birth_year_optional),
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
          label = stringResource(R.string.field_city),
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
          label = stringResource(R.string.field_state),
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
      label = stringResource(R.string.field_pin_code_optional),
      leadingIcon = Icons.Default.Pin,
      error = state.pinCodeError?.message(context, R.string.label_pin_code),
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Done,
      onImeAction = { viewModel.submitProfile() },
      enabled = !state.isSubmitting,
    )

    Spacer(modifier = Modifier.height(32.dp))
    SubmitButton(
      text = stringResource(R.string.action_continue),
      isSubmitting = state.isSubmitting,
      onClick = { viewModel.submitProfile() },
    )
    Spacer(modifier = Modifier.height(24.dp))
  }
}

/**
 * Asks for the permissions the app genuinely uses, through the real system dialogs.
 *
 * This screen used to describe permissions and then request nothing at all, so stringResource(R.string.action_allow) and stringResource(R.string.action_skip)
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
      // stringResource(R.string.action_dont_ask_again), which is the only case where Settings is the honest next step.
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
      title = stringResource(R.string.perm_location_title),
      description = stringResource(R.string.perm_location_body),
      granted = locationGranted,
    )

    Spacer(modifier = Modifier.height(32.dp))

    PermissionRow(
      icon = Icons.Default.NotificationsActive,
      iconTint = FixTheme.colors.accentGraphic,
      background = LightOrangeBorder,
      title = stringResource(R.string.perm_notifications_title),
      description = stringResource(R.string.perm_notifications_body),
      granted = notificationsGranted,
    )

    if (deniedPermanently) {
      Spacer(modifier = Modifier.height(24.dp))
      FormErrorBanner(
        stringResource(R.string.perm_denied_notice)
      )
    }

    Spacer(modifier = Modifier.weight(1f))

    if (allHandled) {
      SubmitButton(text = stringResource(R.string.action_continue), isSubmitting = false, onClick = onPermissionsHandled)
    } else if (deniedPermanently) {
      SubmitButton(
        text = stringResource(R.string.action_open_settings),
        isSubmitting = false,
        onClick = { context.openAppSettings() },
      )
    } else {
      SubmitButton(
        text = stringResource(R.string.action_allow_permissions),
        isSubmitting = false,
        onClick = { launcher.launch(requiredPermissions()) },
      )
    }

    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = onPermissionsHandled) {
      Text(
        if (allHandled) stringResource(R.string.action_skip) else stringResource(R.string.action_continue_without),
        color = FixTheme.colors.textSecondary,
        fontWeight = FontWeight.Medium,
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      stringResource(R.string.perm_change_later),
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
          contentDescription = stringResource(R.string.perm_granted),
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
