package com.example.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import com.example.ui.theme.*
import com.example.util.FieldError
import com.example.util.Validators
import com.example.util.formatIndianMobile
import com.example.util.normalizeMobile
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RESEND_COOLDOWN_SECONDS = 60

@Composable
fun MobileLoginScreen(onSendOtp: (mobile: String) -> Unit, onBack: () -> Unit) {
  var mobile by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<FieldError?>(null) }
  val focusManager = LocalFocusManager.current
  // Localized by LocalizedContent, so messages resolved here honour the chosen language.
  val context = LocalContext.current

  fun submit() {
    val validation = Validators.validateMobile(mobile)
    error = validation
    if (validation == null) {
      focusManager.clearFocus()
      onSendOtp(normalizeMobile(mobile))
    }
  }

  AuthScreen(onBack = onBack) {
    AuthHeader(stringResource(R.string.mobile_login_title), stringResource(R.string.mobile_login_subtitle))
    Spacer(modifier = Modifier.height(Spacing.xl))

    DemoModeNotice(stringResource(R.string.demo_no_sms))

    Spacer(modifier = Modifier.height(Spacing.xl))

    // The country code is a prefix inside the field. As a separate read-only box beside it, the
    // two fields sat at different heights because only one of them had a floating label.
    AuthTextField(
      value = mobile,
      onValueChange = {
        mobile = it
        error = null
      },
      label = stringResource(R.string.field_phone_number),
      leadingIcon = Icons.Default.Phone,
      prefix = "+91 ",
      error = error?.message(context, R.string.label_mobile_number),
      keyboardType = KeyboardType.Phone,
      imeAction = ImeAction.Done,
      onImeAction = { submit() },
    )

    Spacer(modifier = Modifier.height(Spacing.xl))
    SubmitButton(text = stringResource(R.string.action_send_otp), isSubmitting = false, onClick = { submit() })
  }
}

@Composable
fun OtpVerificationScreen(
  viewModel: AuthViewModel,
  mobile: String,
  onVerifiedExistingAccount: () -> Unit,
  onNoAccountForMobile: (mobile: String) -> Unit,
  onBack: () -> Unit,
) {
  // Regenerated on stringResource(R.string.action_resend), so a stale code stops working the moment a new one is issued.
  var generation by remember { mutableIntStateOf(0) }
  val expectedCode = remember(generation) { generateDemoOtp() }
  var code by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }
  var secondsRemaining by remember(generation) { mutableIntStateOf(RESEND_COOLDOWN_SECONDS) }

  val codeFocus = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current
  // Localized by LocalizedContent, so messages resolved here honour the chosen language.
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  LaunchedEffect(generation) {
    while (secondsRemaining > 0) {
      delay(1000)
      secondsRemaining--
    }
  }

  fun verify() {
    if (isSubmitting) return
    val entered = code
    val validation = Validators.validateOtp(entered)
    if (validation != null) {
      error = validation.message(context, R.string.label_code)
      return
    }
    if (entered != expectedCode) {
      error = FormError.OTP_MISMATCH.message(context)
      return
    }
    error = null
    isSubmitting = true
    focusManager.clearFocus()
    scope.launch {
      if (viewModel.signInWithVerifiedMobile(mobile)) {
        onVerifiedExistingAccount()
      } else {
        // Verifying a number proves it is theirs; it does not conjure an account. They still need
        // to give a name and a password, so send them to sign-up with the number carried over.
        onNoAccountForMobile(mobile)
      }
      isSubmitting = false
    }
  }

  AuthScreen(onBack = if (isSubmitting) null else onBack) {
    AuthHeader(
      stringResource(R.string.otp_title),
      stringResource(R.string.otp_subtitle, Validators.OTP_LENGTH, formatIndianMobile(mobile)),
    )

    Spacer(modifier = Modifier.height(Spacing.xl))
    DemoModeNotice(stringResource(R.string.demo_your_code, expectedCode))

    Spacer(modifier = Modifier.height(Spacing.xl))
    OtpCodeInput(
      value = code,
      onValueChange = {
        code = it
        error = null
        if (it.length == Validators.OTP_LENGTH) focusManager.clearFocus()
      },
      length = Validators.OTP_LENGTH,
      isError = error != null,
      enabled = !isSubmitting,
      onDone = { verify() },
      focusRequester = codeFocus,
    )

    if (error != null) {
      Spacer(modifier = Modifier.height(Spacing.sm))
      Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    Spacer(modifier = Modifier.height(Spacing.lg))
    Text(
      text =
        if (secondsRemaining > 0) stringResource(R.string.otp_resend_in, secondsRemaining) else stringResource(R.string.otp_resend),
      style = MaterialTheme.typography.titleSmall,
      color = if (secondsRemaining > 0) FixTheme.colors.textSecondary else FixTheme.colors.primary,
      textAlign = TextAlign.Center,
      // Padding inside the clickable, so the tappable area reaches 48dp instead of the ~20dp
      // the bare text occupied.
      modifier =
        Modifier.align(Alignment.CenterHorizontally)
          .clip(RoundedCornerShape(Radius.sm))
          .clickable(enabled = secondsRemaining == 0 && !isSubmitting, role = Role.Button) {
            code = ""
            error = null
            generation++
          }
          .padding(horizontal = Spacing.md, vertical = 14.dp),
    )

    Spacer(modifier = Modifier.height(Spacing.lg))
    SubmitButton(text = stringResource(R.string.action_verify), isSubmitting = isSubmitting, onClick = { verify() })

    Spacer(modifier = Modifier.height(Spacing.sm))
    TextButton(
      onClick = onBack,
      enabled = !isSubmitting,
      modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = MinTouchTarget),
    ) {
      Text(stringResource(R.string.action_edit_mobile), style = MaterialTheme.typography.labelLarge, color = FixTheme.colors.textSecondary)
    }
  }

  LaunchedEffect(Unit) { codeFocus.requestFocus() }
}

/**
 * Password reset in three real steps. Previously steps 2 and 3 rendered fields hard-wired to
 * `value = ""` with an empty `onValueChange`, so nothing could be typed and no password was ever
 * actually changed.
 */
@Composable
fun ForgotPasswordScreen(
  viewModel: AuthViewModel,
  onPasswordReset: () -> Unit,
  onBack: () -> Unit,
) {
  var step by remember { mutableIntStateOf(1) }
  var mobile by remember { mutableStateOf("") }
  var mobileError by remember { mutableStateOf<String?>(null) }

  var generation by remember { mutableIntStateOf(0) }
  val expectedCode = remember(generation) { generateDemoOtp() }
  var otp by remember { mutableStateOf("") }
  var otpError by remember { mutableStateOf<String?>(null) }

  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var newPasswordError by remember { mutableStateOf<String?>(null) }
  var confirmPasswordError by remember { mutableStateOf<String?>(null) }

  var isSubmitting by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  // Localized by LocalizedContent, so messages resolved here honour the chosen language.
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  fun submitMobile() {
    val validation = Validators.validateMobile(mobile)
    if (validation != null) {
      mobileError = validation.message(context, R.string.label_mobile_number)
      return
    }
    isSubmitting = true
    scope.launch {
      if (viewModel.accountExists(mobile)) {
        mobileError = null
        generation++
        step = 2
      } else {
        mobileError = FormError.NO_ACCOUNT_FOR_MOBILE.message(context)
      }
      isSubmitting = false
    }
  }

  fun submitOtp() {
    val validation = Validators.validateOtp(otp)
    otpError =
      when {
        validation != null -> validation.message(context, R.string.label_code)
        otp.filter { it.isDigit() } != expectedCode -> FormError.OTP_MISMATCH.message(context)
        else -> null
      }
    if (otpError == null) step = 3
  }

  fun submitNewPassword() {
    val passwordValidation = Validators.validatePassword(newPassword)
    val confirmValidation =
      Validators.validatePasswordConfirmation(newPassword, confirmPassword)
    newPasswordError = passwordValidation?.message(context, R.string.label_password)
    confirmPasswordError = confirmValidation?.message(context, R.string.label_confirmation)
    if (passwordValidation != null || confirmValidation != null) return

    isSubmitting = true
    focusManager.clearFocus()
    scope.launch {
      val reset = viewModel.resetPassword(mobile, newPassword)
      isSubmitting = false
      if (reset) onPasswordReset() else newPasswordError = FormError.UNEXPECTED.message(context)
    }
  }

  AuthScreen(onBack = onBack) {
    when (step) {
      1 -> {
        AuthHeader(stringResource(R.string.forgot_title), stringResource(R.string.forgot_subtitle))
        Spacer(modifier = Modifier.height(Spacing.xxl))
        AuthTextField(
          value = mobile,
          onValueChange = {
            mobile = it
            mobileError = null
          },
          label = stringResource(R.string.field_mobile_number),
          leadingIcon = Icons.Default.Phone,
          error = mobileError,
          keyboardType = KeyboardType.Phone,
          imeAction = ImeAction.Done,
          onImeAction = { submitMobile() },
          enabled = !isSubmitting,
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        SubmitButton(text = stringResource(R.string.action_send_otp), isSubmitting = isSubmitting, onClick = { submitMobile() })
      }
      2 -> {
        AuthHeader(
          stringResource(R.string.action_verify_otp),
          stringResource(R.string.code_sent_to, formatIndianMobile(normalizeMobile(mobile))),
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        DemoModeNotice(stringResource(R.string.demo_your_code, expectedCode))
        Spacer(modifier = Modifier.height(Spacing.xl))
        Text(stringResource(R.string.enter_otp), style = MaterialTheme.typography.titleSmall, color = FixTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(Spacing.sm))
        OtpCodeInput(
          value = otp,
          onValueChange = {
            otp = it
            otpError = null
          },
          length = Validators.OTP_LENGTH,
          isError = otpError != null,
          onDone = { submitOtp() },
        )
        if (otpError != null) {
          Spacer(modifier = Modifier.height(Spacing.sm))
          Text(otpError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(Spacing.xl))
        SubmitButton(text = stringResource(R.string.action_verify), isSubmitting = false, onClick = { submitOtp() })
      }
      else -> {
        AuthHeader(stringResource(R.string.reset_title), stringResource(R.string.reset_subtitle))
        Spacer(modifier = Modifier.height(Spacing.xxl))
        AuthTextField(
          value = newPassword,
          onValueChange = {
            newPassword = it
            newPasswordError = null
          },
          label = stringResource(R.string.field_new_password),
          leadingIcon = Icons.Default.Lock,
          error = newPasswordError,
          supportingText =
            stringResource(R.string.password_requirement, Validators.MIN_PASSWORD_LENGTH),
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Next,
          onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
          enabled = !isSubmitting,
          visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
          value = confirmPassword,
          onValueChange = {
            confirmPassword = it
            confirmPasswordError = null
          },
          label = stringResource(R.string.field_confirm_password),
          leadingIcon = Icons.Default.Lock,
          error = confirmPasswordError,
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done,
          onImeAction = { submitNewPassword() },
          enabled = !isSubmitting,
          visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        SubmitButton(
          text = stringResource(R.string.action_save_new_password),
          isSubmitting = isSubmitting,
          onClick = { submitNewPassword() },
        )
      }
    }
  }
}

// -------------------------------------------------------------------- shared pieces

/**
 * States plainly that this build has no SMS gateway.
 *
 * Showing stringResource(R.string.otp_sent_toast) when nothing was sent would leave the user waiting for a
 * message that is never coming.
 */
@Composable
private fun DemoModeNotice(message: String) {
  Surface(
    color = FixTheme.colors.primary.copy(alpha = 0.08f),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.Top) {
      Icon(
        Icons.Outlined.Info,
        contentDescription = null,
        tint = FixTheme.colors.primary,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(Spacing.md))
      Column {
        Text(stringResource(R.string.demo_mode), style = MaterialTheme.typography.titleSmall, color = FixTheme.colors.textPrimary)
        Text(message, style = MaterialTheme.typography.bodySmall, color = FixTheme.colors.textSecondary)
      }
    }
  }
}

private fun generateDemoOtp(): String =
  (1..Validators.OTP_LENGTH).map { Random.nextInt(0, 10) }.joinToString("")
