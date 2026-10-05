package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FixButton
import com.example.ui.components.FixButtonStyle
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import com.example.ui.theme.*

@Composable
fun SignInScreen(
  viewModel: AuthViewModel,
  onLoginSuccess: () -> Unit,
  onMobileLoginClick: () -> Unit,
  onCreateAccountClick: () -> Unit,
  onForgotPasswordClick: () -> Unit,
  onBack: (() -> Unit)? = null,
) {
  val state by viewModel.signIn.collectAsState()
  var passwordVisible by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  // Localized by LocalizedContent, so messages resolved here honour the chosen language.
  val context = LocalContext.current

  // Navigation is driven by the sign-in actually succeeding, not by the button being pressed.
  LaunchedEffect(state.signedIn) {
    if (state.signedIn) onLoginSuccess()
  }

  AuthScreen(onBack = onBack) {
    AuthHeader(stringResource(R.string.signin_title), stringResource(R.string.signin_subtitle))
    Spacer(modifier = Modifier.height(Spacing.xxl))

    state.formError?.let {
      FormErrorBanner(it.message(context))
      Spacer(modifier = Modifier.height(16.dp))
    }

    AuthTextField(
      value = state.mobile,
      onValueChange = viewModel::onSignInMobileChange,
      label = stringResource(R.string.field_mobile_number),
      leadingIcon = Icons.Default.Phone,
      error = state.mobileError?.message(context, R.string.label_mobile_number),
      keyboardType = KeyboardType.Phone,
      imeAction = ImeAction.Next,
      onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
      enabled = !state.isSubmitting,
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.password,
      onValueChange = viewModel::onSignInPasswordChange,
      label = stringResource(R.string.field_password),
      leadingIcon = Icons.Default.Lock,
      error = state.passwordError?.message(context, R.string.label_password),
      keyboardType = KeyboardType.Password,
      imeAction = ImeAction.Done,
      onImeAction = {
        focusManager.clearFocus()
        viewModel.submitSignIn()
      },
      enabled = !state.isSubmitting,
      visualTransformation =
        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
          Icon(
            imageVector =
              if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
            contentDescription = if (passwordVisible) stringResource(R.string.cd_hide_password) else stringResource(R.string.cd_show_password),
          )
        }
      },
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      // No end padding, so the text lines up with the field edge above it.
      TextButton(
        onClick = onForgotPasswordClick,
        enabled = !state.isSubmitting,
        contentPadding = PaddingValues(start = Spacing.md, end = 0.dp),
      ) {
        Text(stringResource(R.string.action_forgot_password), style = MaterialTheme.typography.titleSmall, color = FixTheme.colors.primary)
      }
    }

    Spacer(modifier = Modifier.height(Spacing.lg))
    SubmitButton(
      text = stringResource(R.string.action_sign_in),
      isSubmitting = state.isSubmitting,
      onClick = {
        focusManager.clearFocus()
        viewModel.submitSignIn()
      },
    )

    Spacer(modifier = Modifier.height(24.dp))
    OrDivider()
    Spacer(modifier = Modifier.height(24.dp))

    AlternativeLoginButton(stringResource(R.string.action_continue_with_mobile_otp), onMobileLoginClick, !state.isSubmitting)

    Spacer(modifier = Modifier.height(Spacing.xl))
    AuthFooterPrompt(
      prompt = stringResource(R.string.signin_no_account),
      action = stringResource(R.string.action_create_new_account),
      onClick = onCreateAccountClick,
      enabled = !state.isSubmitting,
    )
  }
}

@Composable
fun CreateAccountScreen(
  viewModel: AuthViewModel,
  prefilledMobile: String,
  onCreateSuccess: () -> Unit,
  onMobileOtpClick: () -> Unit,
  onBack: () -> Unit,
) {
  val state by viewModel.signUp.collectAsState()
  var passwordVisible by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  // Localized by LocalizedContent, so messages resolved here honour the chosen language.
  val context = LocalContext.current

  LaunchedEffect(prefilledMobile) { viewModel.prefillSignUpMobile(prefilledMobile) }

  LaunchedEffect(state.registered) {
    if (state.registered) onCreateSuccess()
  }

  AuthScreen(onBack = onBack) {
    AuthHeader(stringResource(R.string.signup_title), stringResource(R.string.signup_subtitle))
    Spacer(modifier = Modifier.height(Spacing.xxl))

    state.formError?.let {
      FormErrorBanner(it.message(context))
      Spacer(modifier = Modifier.height(16.dp))
    }

    AuthTextField(
      value = state.fullName,
      onValueChange = viewModel::onSignUpFullNameChange,
      label = stringResource(R.string.field_full_name),
      leadingIcon = Icons.Default.Person,
      error = state.fullNameError?.message(context, R.string.label_full_name),
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Next,
      onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
      enabled = !state.isSubmitting,
    )
    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.mobile,
      onValueChange = viewModel::onSignUpMobileChange,
      label = stringResource(R.string.field_mobile_number),
      leadingIcon = Icons.Default.Phone,
      error = state.mobileError?.message(context, R.string.label_mobile_number),
      keyboardType = KeyboardType.Phone,
      imeAction = ImeAction.Next,
      onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
      enabled = !state.isSubmitting,
    )
    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.email,
      onValueChange = viewModel::onSignUpEmailChange,
      label = stringResource(R.string.field_email_optional),
      leadingIcon = Icons.Outlined.Email,
      error = state.emailError?.message(context, R.string.label_email),
      keyboardType = KeyboardType.Email,
      imeAction = ImeAction.Next,
      onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
      enabled = !state.isSubmitting,
    )
    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.password,
      onValueChange = viewModel::onSignUpPasswordChange,
      label = stringResource(R.string.field_password),
      leadingIcon = Icons.Default.Lock,
      error = state.passwordError?.message(context, R.string.label_password),
      supportingText =
        stringResource(R.string.password_requirement, com.example.util.Validators.MIN_PASSWORD_LENGTH),
      keyboardType = KeyboardType.Password,
      imeAction = ImeAction.Next,
      onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
      enabled = !state.isSubmitting,
      visualTransformation =
        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
          Icon(
            imageVector =
              if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
            contentDescription = if (passwordVisible) stringResource(R.string.cd_hide_password) else stringResource(R.string.cd_show_password),
          )
        }
      },
    )
    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.confirmPassword,
      onValueChange = viewModel::onSignUpConfirmPasswordChange,
      label = stringResource(R.string.field_confirm_password),
      leadingIcon = Icons.Default.Lock,
      error = state.confirmPasswordError?.message(context, R.string.label_confirmation),
      keyboardType = KeyboardType.Password,
      imeAction = ImeAction.Done,
      onImeAction = {
        focusManager.clearFocus()
        viewModel.submitSignUp()
      },
      enabled = !state.isSubmitting,
      visualTransformation = PasswordVisualTransformation(),
    )

    Spacer(modifier = Modifier.height(Spacing.sm))
    // The whole row is the toggle, and the box sits flush with the fields above instead of being
    // indented by the checkbox's own 48dp touch padding.
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(Radius.sm))
          .toggleable(
            value = state.agreedToTerms,
            enabled = !state.isSubmitting,
            role = Role.Checkbox,
            onValueChange = viewModel::onSignUpTermsChange,
          )
          .heightIn(min = MinTouchTarget),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        checked = state.agreedToTerms,
        onCheckedChange = null,
        enabled = !state.isSubmitting,
        colors =
          CheckboxDefaults.colors(
            checkedColor = FixTheme.colors.primary,
            uncheckedColor = if (state.termsNotAccepted) FixTheme.colors.danger else FixTheme.colors.textSecondary,
          ),
      )
      Spacer(modifier = Modifier.width(Spacing.md))
      Text(stringResource(R.string.terms_agree), style = MaterialTheme.typography.bodyMedium, color = FixTheme.colors.textPrimary)
    }
    if (state.termsNotAccepted) {
      Text(
        stringResource(R.string.terms_required),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
      )
    }

    Spacer(modifier = Modifier.height(24.dp))
    SubmitButton(
      text = stringResource(R.string.action_create_account),
      isSubmitting = state.isSubmitting,
      onClick = {
        focusManager.clearFocus()
        viewModel.submitSignUp()
      },
    )

    Spacer(modifier = Modifier.height(24.dp))
    OrDivider()
    Spacer(modifier = Modifier.height(24.dp))

    AlternativeLoginButton(stringResource(R.string.action_signup_with_otp), onMobileOtpClick, !state.isSubmitting)

    Spacer(modifier = Modifier.height(Spacing.xl))
    AuthFooterPrompt(
      prompt = stringResource(R.string.signup_have_account),
      action = stringResource(R.string.action_sign_in),
      onClick = onBack,
      enabled = !state.isSubmitting,
    )
  }
}

// -------------------------------------------------------------------- shared pieces

/**
 * A text field that reserves room for its error, so showing one does not shift the rest of the
 * form downwards while the user is reading it.
 */
@Composable
fun AuthTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
  error: String?,
  keyboardType: KeyboardType,
  imeAction: ImeAction,
  onImeAction: () -> Unit,
  enabled: Boolean = true,
  supportingText: String? = null,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  trailingIcon: (@Composable () -> Unit)? = null,
  prefix: String? = null,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      leadingIcon = { Icon(leadingIcon, contentDescription = null) },
      trailingIcon = trailingIcon,
      prefix = prefix?.let { { Text(it, color = FixTheme.colors.textPrimary) } },
      isError = error != null,
      enabled = enabled,
      singleLine = true,
      visualTransformation = visualTransformation,
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
      keyboardActions =
        androidx.compose.foundation.text.KeyboardActions(
          onNext = { onImeAction() },
          onDone = { onImeAction() },
        ),
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(Radius.md),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedBorderColor = FixTheme.colors.primary,
          unfocusedBorderColor = FixTheme.colors.border,
          focusedLabelColor = FixTheme.colors.primary,
          unfocusedLabelColor = FixTheme.colors.textSecondary,
          focusedLeadingIconColor = FixTheme.colors.primary,
          unfocusedLeadingIconColor = FixTheme.colors.textSecondary,
          cursorColor = FixTheme.colors.primary,
        ),
    )
    val helper = error ?: supportingText
    if (helper != null) {
      Text(
        text = helper,
        color = if (error != null) MaterialTheme.colorScheme.error else FixTheme.colors.textSecondary,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = Spacing.lg, top = Spacing.xs),
      )
    }
  }
}

/** A button that cannot be pressed twice while its work is still in flight. */
@Composable
fun SubmitButton(text: String, isSubmitting: Boolean, onClick: () -> Unit, enabled: Boolean = true) {
  FixButton(text = text, onClick = onClick, enabled = enabled, isLoading = isSubmitting)
}

@Composable
fun FormErrorBanner(message: String) {
  Surface(
    color = MaterialTheme.colorScheme.errorContainer,
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(
        Icons.Default.ErrorOutline,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = message,
        color = MaterialTheme.colorScheme.onErrorContainer,
        fontSize = 14.sp,
      )
    }
  }
}

@Composable
fun OrDivider() {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    HorizontalDivider(modifier = Modifier.weight(1f), color = FixTheme.colors.border)
    Text(
      stringResource(R.string.divider_or),
      style = MaterialTheme.typography.labelMedium,
      color = FixTheme.colors.textSecondary,
      modifier = Modifier.padding(horizontal = Spacing.lg),
    )
    HorizontalDivider(modifier = Modifier.weight(1f), color = FixTheme.colors.border)
  }
}

@Composable
fun AlternativeLoginButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
  FixButton(text = text, onClick = onClick, enabled = enabled, style = FixButtonStyle.SECONDARY)
}
