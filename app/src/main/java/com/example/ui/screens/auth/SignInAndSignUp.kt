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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

  // Navigation is driven by the sign-in actually succeeding, not by the button being pressed.
  LaunchedEffect(state.signedIn) {
    if (state.signedIn) onLoginSuccess()
  }

  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
  ) {
    Spacer(modifier = Modifier.height(40.dp))
    Text("Welcome Back", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Sign in to continue", fontSize = 16.sp, color = SecondaryGrey)
    Spacer(modifier = Modifier.height(32.dp))

    state.formError?.let {
      FormErrorBanner(it.message())
      Spacer(modifier = Modifier.height(16.dp))
    }

    AuthTextField(
      value = state.mobile,
      onValueChange = viewModel::onSignInMobileChange,
      label = "Mobile Number",
      leadingIcon = Icons.Default.Phone,
      error = state.mobileError?.message("Mobile number"),
      keyboardType = KeyboardType.Phone,
      imeAction = ImeAction.Next,
      onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
      enabled = !state.isSubmitting,
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.password,
      onValueChange = viewModel::onSignInPasswordChange,
      label = "Password",
      leadingIcon = Icons.Default.Lock,
      error = state.passwordError?.message("Password"),
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
            contentDescription = if (passwordVisible) "Hide password" else "Show password",
          )
        }
      },
    )

    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      TextButton(onClick = onForgotPasswordClick, enabled = !state.isSubmitting) {
        Text("Forgot Password?", color = BluePrimary, fontWeight = FontWeight.SemiBold)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    SubmitButton(
      text = "Sign In",
      isSubmitting = state.isSubmitting,
      onClick = {
        focusManager.clearFocus()
        viewModel.submitSignIn()
      },
    )

    Spacer(modifier = Modifier.height(24.dp))
    OrDivider()
    Spacer(modifier = Modifier.height(24.dp))

    AlternativeLoginButton("Continue with Mobile OTP", onMobileLoginClick, !state.isSubmitting)

    Spacer(modifier = Modifier.height(32.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      Text("Don't have an account? ", color = SecondaryGrey)
      Text(
        "Create New Account",
        color = BluePrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable(enabled = !state.isSubmitting) { onCreateAccountClick() },
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

  LaunchedEffect(prefilledMobile) { viewModel.prefillSignUpMobile(prefilledMobile) }

  LaunchedEffect(state.registered) {
    if (state.registered) onCreateSuccess()
  }

  Column(
    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
  ) {
    Spacer(modifier = Modifier.height(24.dp))
    Text("Create Your Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
    Spacer(modifier = Modifier.height(24.dp))

    state.formError?.let {
      FormErrorBanner(it.message())
      Spacer(modifier = Modifier.height(16.dp))
    }

    AuthTextField(
      value = state.fullName,
      onValueChange = viewModel::onSignUpFullNameChange,
      label = "Full Name",
      leadingIcon = Icons.Default.Person,
      error = state.fullNameError?.message("Full name"),
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Next,
      onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
      enabled = !state.isSubmitting,
    )
    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.mobile,
      onValueChange = viewModel::onSignUpMobileChange,
      label = "Mobile Number",
      leadingIcon = Icons.Default.Phone,
      error = state.mobileError?.message("Mobile number"),
      keyboardType = KeyboardType.Phone,
      imeAction = ImeAction.Next,
      onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
      enabled = !state.isSubmitting,
    )
    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.email,
      onValueChange = viewModel::onSignUpEmailChange,
      label = "Email Address (Optional)",
      leadingIcon = Icons.Outlined.Email,
      error = state.emailError?.message("Email"),
      keyboardType = KeyboardType.Email,
      imeAction = ImeAction.Next,
      onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
      enabled = !state.isSubmitting,
    )
    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.password,
      onValueChange = viewModel::onSignUpPasswordChange,
      label = "Password",
      leadingIcon = Icons.Default.Lock,
      error = state.passwordError?.message("Password"),
      supportingText =
        "At least ${com.example.util.Validators.MIN_PASSWORD_LENGTH} characters, with a letter and a number",
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
            contentDescription = if (passwordVisible) "Hide password" else "Show password",
          )
        }
      },
    )
    Spacer(modifier = Modifier.height(16.dp))

    AuthTextField(
      value = state.confirmPassword,
      onValueChange = viewModel::onSignUpConfirmPasswordChange,
      label = "Confirm Password",
      leadingIcon = Icons.Default.Lock,
      error = state.confirmPasswordError?.message("Confirmation"),
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
        colors = CheckboxDefaults.colors(checkedColor = BluePrimary),
      )
      Text("I agree to the Terms & Privacy Policy", fontSize = 14.sp, color = DarkNavy)
    }
    if (state.termsNotAccepted) {
      Text(
        "Please accept the Terms & Privacy Policy to continue",
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 12.dp),
      )
    }

    Spacer(modifier = Modifier.height(24.dp))
    SubmitButton(
      text = "Create Account",
      isSubmitting = state.isSubmitting,
      onClick = {
        focusManager.clearFocus()
        viewModel.submitSignUp()
      },
    )

    Spacer(modifier = Modifier.height(24.dp))
    OrDivider()
    Spacer(modifier = Modifier.height(24.dp))

    AlternativeLoginButton("Sign Up with Mobile OTP", onMobileOtpClick, !state.isSubmitting)

    Spacer(modifier = Modifier.height(32.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
      Text("Already have an account? ", color = SecondaryGrey)
      Text(
        "Sign In",
        color = BluePrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable(enabled = !state.isSubmitting) { onBack() },
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
          focusedBorderColor = BluePrimary,
          unfocusedBorderColor = BorderGrey,
        ),
    )
    val helper = error ?: supportingText
    if (helper != null) {
      Text(
        text = helper,
        color = if (error != null) MaterialTheme.colorScheme.error else SecondaryGrey,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
      )
    }
  }
}

/** A button that cannot be pressed twice while its work is still in flight. */
@Composable
fun SubmitButton(text: String, isSubmitting: Boolean, onClick: () -> Unit, enabled: Boolean = true) {
  Button(
    onClick = onClick,
    enabled = enabled && !isSubmitting,
    modifier = Modifier.fillMaxWidth().height(56.dp),
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
  ) {
    if (isSubmitting) {
      CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        color = Color.White,
        strokeWidth = 2.dp,
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text("Please wait…", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    } else {
      Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
  }
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
    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGrey)
    Text(" OR ", color = SecondaryGrey, modifier = Modifier.padding(horizontal = 8.dp))
    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGrey)
  }
}

@Composable
fun AlternativeLoginButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    modifier = Modifier.fillMaxWidth().height(56.dp),
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.dp, BorderGrey),
    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavy),
  ) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
  }
}
