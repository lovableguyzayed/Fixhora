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
import com.example.ui.theme.*

@Composable
fun SignInScreen(
  viewModel: AuthViewModel,
  onLoginSuccess: () -> Unit,
  onMobileLoginClick: () -> Unit,
  onCreateAccountClick: () -> Unit,
  onForgotPasswordClick: () -> Unit,
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

  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
  ) {
    Spacer(modifier = Modifier.height(40.dp))
    Text(stringResource(R.string.signin_title), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary)
    Spacer(modifier = Modifier.height(8.dp))
    Text(stringResource(R.string.signin_subtitle), fontSize = 16.sp, color = FixTheme.colors.textSecondary)
    Spacer(modifier = Modifier.height(32.dp))

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

    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      TextButton(onClick = onForgotPasswordClick, enabled = !state.isSubmitting) {
        Text(stringResource(R.string.action_forgot_password), color = FixTheme.colors.primary, fontWeight = FontWeight.SemiBold)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
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

    Spacer(modifier = Modifier.height(32.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(stringResource(R.string.signin_no_account), color = FixTheme.colors.textSecondary)
      Text(
        stringResource(R.string.action_create_new_account),
        color = FixTheme.colors.primary,
        fontWeight = FontWeight.Bold,
        // Padding sits inside the clickable, so it grows the touch target rather than just the
        // gap around it. The bare text was about 20dp tall — under half the 48dp minimum.
        modifier =
          Modifier.clip(RoundedCornerShape(Radius.sm))
            .clickable(enabled = !state.isSubmitting, role = Role.Button) { onCreateAccountClick() }
            .padding(horizontal = Spacing.sm, vertical = 14.dp),
      )
    }
    Spacer(modifier = Modifier.height(24.dp))
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

  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
  ) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(stringResource(R.string.signup_title), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary)
    Spacer(modifier = Modifier.height(24.dp))

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

    Spacer(modifier = Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
        checked = state.agreedToTerms,
        onCheckedChange = viewModel::onSignUpTermsChange,
        enabled = !state.isSubmitting,
        colors = CheckboxDefaults.colors(checkedColor = FixTheme.colors.primary),
      )
      Text(stringResource(R.string.terms_agree), fontSize = 14.sp, color = FixTheme.colors.textPrimary)
    }
    if (state.termsNotAccepted) {
      Text(
        stringResource(R.string.terms_required),
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 12.dp),
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

    Spacer(modifier = Modifier.height(32.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(stringResource(R.string.signup_have_account), color = FixTheme.colors.textSecondary)
      Text(
        stringResource(R.string.action_sign_in),
        color = FixTheme.colors.primary,
        fontWeight = FontWeight.Bold,
        modifier =
          Modifier.clip(RoundedCornerShape(Radius.sm))
            .clickable(enabled = !state.isSubmitting, role = Role.Button) { onBack() }
            .padding(horizontal = Spacing.sm, vertical = 14.dp),
      )
    }
    Spacer(modifier = Modifier.height(24.dp))
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
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      leadingIcon = { Icon(leadingIcon, contentDescription = null) },
      trailingIcon = trailingIcon,
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
      shape = RoundedCornerShape(12.dp),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedBorderColor = FixTheme.colors.primary,
          unfocusedBorderColor = FixTheme.colors.border,
        ),
    )
    val helper = error ?: supportingText
    if (helper != null) {
      Text(
        text = helper,
        color = if (error != null) MaterialTheme.colorScheme.error else FixTheme.colors.textSecondary,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
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
    Text(stringResource(R.string.divider_or), color = FixTheme.colors.textSecondary, modifier = Modifier.padding(horizontal = 8.dp))
    HorizontalDivider(modifier = Modifier.weight(1f), color = FixTheme.colors.border)
  }
}

@Composable
fun AlternativeLoginButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
  FixButton(text = text, onClick = onClick, enabled = enabled, style = FixButtonStyle.SECONDARY)
}
