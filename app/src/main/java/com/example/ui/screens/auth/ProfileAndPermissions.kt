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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.semantics.Role
import com.example.ui.components.FixCard
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

  AuthScreen(onBack = null) {
    AuthHeader(stringResource(R.string.profile_title), stringResource(R.string.profile_subtitle))
    Spacer(modifier = Modifier.height(Spacing.xxl))

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
    Spacer(modifier = Modifier.height(Spacing.lg))

    // Gender was a free-text field squeezed into half the width, where its label wrapped onto three
    // lines. A fixed choice is quicker and stores a stable English key whatever the UI language.
    Text(
      stringResource(R.string.field_gender_optional),
      style = MaterialTheme.typography.titleSmall,
      color = FixTheme.colors.textPrimary,
    )
    Spacer(modifier = Modifier.height(Spacing.sm))
    GenderChoice(
      selected = state.gender,
      enabled = !state.isSubmitting,
      onSelect = { viewModel.onProfileFieldChange(gender = it) },
    )
    Spacer(modifier = Modifier.height(Spacing.lg))

    AuthTextField(
      value = state.dateOfBirth,
      onValueChange = { viewModel.onProfileFieldChange(dateOfBirth = it.filter(Char::isDigit).take(4)) },
      label = stringResource(R.string.field_birth_year_optional),
      leadingIcon = Icons.Default.Cake,
      error = null,
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Next,
      onImeAction = {},
      enabled = !state.isSubmitting,
    )
    Spacer(modifier = Modifier.height(Spacing.lg))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
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
    Spacer(modifier = Modifier.height(Spacing.lg))

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

    Spacer(modifier = Modifier.height(Spacing.xxl))
    SubmitButton(
      text = stringResource(R.string.action_continue),
      isSubmitting = state.isSubmitting,
      onClick = { viewModel.submitProfile() },
    )
  }
}

/** Stored values stay English keys, so changing the app language never changes the saved data. */
private val GenderOptions =
  listOf("Male" to R.string.gender_male, "Female" to R.string.gender_female, "Other" to R.string.gender_other)

@Composable
private fun GenderChoice(selected: String, enabled: Boolean, onSelect: (String) -> Unit) {
  val colors = FixTheme.colors
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
    GenderOptions.forEach { (value, label) ->
      val isSelected = selected.equals(value, ignoreCase = true)
      val shape = RoundedCornerShape(Radius.md)
      Surface(
        modifier =
          Modifier.weight(1f)
            .height(MinTouchTarget)
            .clip(shape)
            .selectable(
              selected = isSelected,
              enabled = enabled,
              role = Role.RadioButton,
              // Tapping the chosen option again clears it: the field is optional.
              onClick = { onSelect(if (isSelected) "" else value) },
            ),
        shape = shape,
        color = if (isSelected) colors.primarySurface else colors.surface,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) colors.primary else colors.border),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            stringResource(label),
            style = MaterialTheme.typography.titleSmall,
            color = if (isSelected) colors.primary else colors.textPrimary,
          )
        }
      }
    }
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
    modifier =
      Modifier.fillMaxSize()
        .background(FixTheme.colors.background)
        .systemBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = Spacing.xl),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(modifier = Modifier.height(Spacing.xxxl))
    Box(
      modifier = Modifier.size(112.dp).clip(CircleShape).background(FixTheme.colors.primarySurface),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier = Modifier.size(64.dp).clip(CircleShape).background(FixTheme.colors.surface),
        contentAlignment = Alignment.Center,
      ) {
        Icon(Icons.Default.Shield, contentDescription = null, tint = FixTheme.colors.primary, modifier = Modifier.size(32.dp))
      }
    }
    Spacer(modifier = Modifier.height(Spacing.xl))
    AuthHeader(
      stringResource(R.string.perm_title),
      stringResource(R.string.perm_subtitle),
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(Spacing.xxl))

    PermissionRow(
      icon = Icons.Default.LocationOn,
      iconTint = FixTheme.colors.primary,
      background = FixTheme.colors.primarySurface,
      title = stringResource(R.string.perm_location_title),
      description = stringResource(R.string.perm_location_body),
      granted = locationGranted,
    )
    Spacer(modifier = Modifier.height(Spacing.md))
    PermissionRow(
      icon = Icons.Default.NotificationsActive,
      iconTint = FixTheme.colors.accentGraphic,
      background = FixTheme.colors.accentSurface,
      title = stringResource(R.string.perm_notifications_title),
      description = stringResource(R.string.perm_notifications_body),
      granted = notificationsGranted,
    )

    if (deniedPermanently) {
      Spacer(modifier = Modifier.height(Spacing.lg))
      FormErrorBanner(stringResource(R.string.perm_denied_notice))
    }

    Spacer(modifier = Modifier.height(Spacing.xxl))

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

    Spacer(modifier = Modifier.height(Spacing.xs))
    TextButton(onClick = onPermissionsHandled, modifier = Modifier.heightIn(min = MinTouchTarget)) {
      Text(
        if (allHandled) stringResource(R.string.action_skip) else stringResource(R.string.action_continue_without),
        style = MaterialTheme.typography.labelLarge,
        color = FixTheme.colors.textSecondary,
      )
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
    Text(
      stringResource(R.string.perm_change_later),
      style = MaterialTheme.typography.bodySmall,
      color = FixTheme.colors.textSecondary,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(Spacing.xl))
  }
}

/** One permission as a card: what it is, why it is asked for, and whether it is already granted. */
@Composable
private fun PermissionRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: androidx.compose.ui.graphics.Color,
  background: androidx.compose.ui.graphics.Color,
  title: String,
  description: String,
  granted: Boolean,
) {
  FixCard {
    Row(modifier = Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(Radius.md)).background(background),
        contentAlignment = Alignment.Center,
      ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
      }
      Spacer(modifier = Modifier.width(Spacing.lg))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = FixTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = FixTheme.colors.textSecondary)
      }
      if (granted) {
        Spacer(modifier = Modifier.width(Spacing.sm))
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = stringResource(R.string.perm_granted),
          tint = FixTheme.colors.success,
          modifier = Modifier.size(22.dp),
        )
      }
    }
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
