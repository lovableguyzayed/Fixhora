package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.session.AppLanguage
import com.example.data.session.ThemePreference
import com.example.ui.components.BentoCard
import com.example.ui.components.FixButton
import com.example.ui.components.FixButtonStyle
import com.example.ui.components.FixTopBar
import com.example.ui.components.InitialsAvatar
import com.example.ui.components.SectionHeader
import com.example.ui.screens.legal.LegalDocument
import com.example.ui.theme.Bento
import com.example.ui.theme.FixTheme
import com.example.ui.theme.MinTouchTarget
import com.example.ui.theme.Radius
import com.example.ui.theme.Spacing
import com.example.util.formatIndianMobile

/**
 * Who you are and how the app behaves, in one place.
 *
 * There was no profile or settings screen at all: the details entered after sign-up could never be
 * changed, the theme and the update check were tucked under the role choice, there was no way to
 * delete an account, and nothing led to the terms or to any help.
 */
@Composable
fun SettingsScreen(
  state: SettingsUiState,
  onBack: () -> Unit,
  onEditProfile: () -> Unit,
  onSignIn: () -> Unit,
  onLanguageChange: (AppLanguage) -> Unit,
  onThemeChange: (ThemePreference) -> Unit,
  onOpenDocument: (LegalDocument) -> Unit,
  onCheckForUpdates: () -> Unit,
  onSignOut: () -> Unit,
  onDeleteAccount: () -> Unit,
  onDismissDeleteError: () -> Unit,
  versionName: String = BuildConfig.VERSION_NAME,
  versionCode: Int = BuildConfig.VERSION_CODE,
) {
  val colors = FixTheme.colors
  var confirmingDelete by rememberSaveable { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxSize().background(colors.background).systemBarsPadding()) {
    FixTopBar(title = stringResource(R.string.settings_title), onBack = onBack)
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = Spacing.xl, vertical = Spacing.sm)
    ) {
      AccountCard(state = state, onEditProfile = onEditProfile, onSignIn = onSignIn)

      SettingsSection(stringResource(R.string.language_heading)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          ChoiceOption(
            text = stringResource(R.string.language_english),
            selected = state.language == AppLanguage.ENGLISH,
            onClick = { onLanguageChange(AppLanguage.ENGLISH) },
            modifier = Modifier.weight(1f),
          )
          ChoiceOption(
            text = stringResource(R.string.language_hindi),
            selected = state.language == AppLanguage.HINDI,
            onClick = { onLanguageChange(AppLanguage.HINDI) },
            modifier = Modifier.weight(1f),
          )
        }
      }

      SettingsSection(stringResource(R.string.theme_heading)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          listOf(
              Triple(ThemePreference.SYSTEM, R.string.theme_system, Icons.Default.BrightnessAuto),
              Triple(ThemePreference.LIGHT, R.string.theme_light, Icons.Default.LightMode),
              Triple(ThemePreference.DARK, R.string.theme_dark, Icons.Default.DarkMode),
            )
            .forEach { (option, label, icon) ->
              ChoiceOption(
                text = stringResource(label),
                selected = state.theme == option,
                onClick = { onThemeChange(option) },
                icon = icon,
                modifier = Modifier.weight(1f),
              )
            }
        }
      }

      SettingsSection(stringResource(R.string.settings_section_help)) {
        SettingsGroup {
          SettingsRow(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            title = stringResource(R.string.settings_help),
            onClick = { onOpenDocument(LegalDocument.HELP) },
          )
          SettingsDivider()
          SettingsRow(
            icon = Icons.Default.Description,
            title = stringResource(R.string.legal_terms_title),
            onClick = { onOpenDocument(LegalDocument.TERMS) },
          )
          SettingsDivider()
          SettingsRow(
            icon = Icons.Default.PrivacyTip,
            title = stringResource(R.string.legal_privacy_title),
            onClick = { onOpenDocument(LegalDocument.PRIVACY) },
          )
        }
      }

      SettingsSection(stringResource(R.string.settings_section_about)) {
        SettingsGroup {
          SettingsRow(
            icon = Icons.Default.SystemUpdate,
            title = stringResource(R.string.settings_check_updates),
            subtitle = stringResource(R.string.settings_version, versionName, versionCode),
            onClick = onCheckForUpdates,
          )
        }
      }

      if (state.isSignedIn) {
        Spacer(Modifier.height(Spacing.xxl))
        FixButton(
          text = stringResource(R.string.action_sign_out),
          onClick = onSignOut,
          style = FixButtonStyle.SECONDARY,
          enabled = !state.isDeleting,
        )
        Spacer(Modifier.height(Spacing.sm))
        TextButton(
          onClick = { confirmingDelete = true },
          enabled = !state.isDeleting,
          modifier = Modifier.fillMaxWidth().heightIn(min = MinTouchTarget),
        ) {
          Text(
            stringResource(R.string.settings_delete_account),
            style = MaterialTheme.typography.titleSmall,
            color = colors.danger,
          )
        }
      }
      Spacer(Modifier.height(Spacing.xxl))
    }
  }

  if (confirmingDelete || state.isDeleting) {
    AlertDialog(
      onDismissRequest = { if (!state.isDeleting) confirmingDelete = false },
      containerColor = colors.surface,
      title = {
        Text(stringResource(R.string.settings_delete_title), color = colors.textPrimary, fontWeight = FontWeight.Bold)
      },
      text = { Text(stringResource(R.string.settings_delete_body), color = colors.textSecondary) },
      confirmButton = {
        TextButton(
          onClick = {
            confirmingDelete = false
            onDeleteAccount()
          },
          enabled = !state.isDeleting,
        ) {
          Text(
            stringResource(if (state.isDeleting) R.string.action_please_wait else R.string.settings_delete_confirm),
            color = colors.danger,
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { confirmingDelete = false }, enabled = !state.isDeleting) {
          Text(stringResource(R.string.update_action_cancel), color = colors.textSecondary)
        }
      },
    )
  }

  if (state.deleteFailed) {
    AlertDialog(
      onDismissRequest = onDismissDeleteError,
      containerColor = colors.surface,
      text = { Text(stringResource(R.string.settings_delete_failed), color = colors.textPrimary) },
      confirmButton = {
        TextButton(onClick = onDismissDeleteError) {
          Text(stringResource(R.string.update_action_ok), color = colors.primary)
        }
      },
    )
  }
}

/** The signed-in person at the top, or an invitation to sign in for a guest. */
@Composable
private fun AccountCard(state: SettingsUiState, onEditProfile: () -> Unit, onSignIn: () -> Unit) {
  val bento = Bento.Sun
  val colors = FixTheme.colors
  BentoCard(colors = bento, modifier = Modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      InitialsAvatar(name = state.name, size = 56.dp)
      Spacer(Modifier.width(Spacing.lg))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = state.name ?: stringResource(R.string.settings_guest_title),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.ExtraBold,
          color = bento.onFill,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        val details =
          if (state.isSignedIn) {
            listOfNotNull(state.mobile?.let(::formatIndianMobile), state.city).joinToString(" · ")
          } else {
            stringResource(R.string.settings_guest_body)
          }
        if (details.isNotEmpty()) {
          Spacer(Modifier.height(Spacing.xs))
          Text(details, style = MaterialTheme.typography.bodyMedium, color = bento.onFillSecondary)
        }
      }
    }
    Spacer(Modifier.height(Spacing.lg))
    FixButton(
      text = stringResource(if (state.isSignedIn) R.string.settings_edit_profile else R.string.settings_sign_in),
      onClick = if (state.isSignedIn) onEditProfile else onSignIn,
      style = FixButtonStyle.INK,
    )
  }
}

/**
 * One of a few side-by-side choices. The icon sits above the label so three fit across a narrow
 * phone in either language; a pill with the icon beside the text cut "System" short.
 */
@Composable
private fun ChoiceOption(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
) {
  val colors = FixTheme.colors
  val shape = RoundedCornerShape(Radius.lg)
  val content = if (selected) colors.onHighlight else colors.textPrimary
  Surface(
    modifier =
      modifier
        .heightIn(min = if (icon != null) 72.dp else MinTouchTarget)
        .clip(shape)
        .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    shape = shape,
    color = if (selected) colors.highlight else colors.surface,
    border = if (selected) null else BorderStroke(1.dp, colors.border),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.sm),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      if (icon != null) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(Spacing.xs))
      }
      Text(text, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
    }
  }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
  Spacer(Modifier.height(Spacing.xl))
  SectionHeader(title = title)
  Spacer(Modifier.height(Spacing.md))
  Column(content = content)
}

/** Rows that belong together, on one rounded card. */
@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
  val colors = FixTheme.colors
  Surface(
    shape = RoundedCornerShape(Radius.lg),
    color = colors.surface,
    border = BorderStroke(1.dp, colors.border),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(content = content)
  }
}

@Composable
private fun SettingsDivider() {
  HorizontalDivider(color = FixTheme.colors.border, modifier = Modifier.padding(start = 64.dp))
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, onClick: () -> Unit, subtitle: String? = null) {
  val colors = FixTheme.colors
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 56.dp)
        .clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(32.dp).clip(CircleShape).background(colors.surfaceAlt),
      contentAlignment = Alignment.Center,
    ) {
      Icon(icon, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
    }
    Spacer(Modifier.width(Spacing.lg))
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
      if (subtitle != null) {
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
      }
    }
    Icon(
      Icons.AutoMirrored.Filled.KeyboardArrowRight,
      contentDescription = null,
      tint = colors.textMuted,
      modifier = Modifier.size(20.dp),
    )
  }
}
