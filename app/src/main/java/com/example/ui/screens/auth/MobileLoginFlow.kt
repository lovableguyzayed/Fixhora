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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

  fun submit() {
    val validation = Validators.validateMobile(mobile)
    error = validation
    if (validation == null) {
      focusManager.clearFocus()
      onSendOtp(normalizeMobile(mobile))
    }
  }

  Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
    BackButton(onBack)
    Spacer(modifier = Modifier.height(24.dp))
    Text(
      "Login with Mobile Number",
      fontSize = 28.sp,
      fontWeight = FontWeight.Bold,
      color = FixTheme.colors.textPrimary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      "Enter your mobile number to receive a verification code.",
      fontSize = 16.sp,
      color = FixTheme.colors.textSecondary,
    )
    Spacer(modifier = Modifier.height(32.dp))

    DemoModeNotice("No SMS is sent in this build. The code appears on the next screen.")

    Spacer(modifier = Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
      OutlinedTextField(
        value = "+91",
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        modifier = Modifier.width(84.dp),
        shape = RoundedCornerShape(12.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Box(modifier = Modifier.weight(1f)) {
        AuthTextField(
          value = mobile,
          onValueChange = {
            mobile = it
            error = null
          },
          label = "Phone Number",
          leadingIcon = Icons.Default.Phone,
          error = error?.message("Mobile number"),
          keyboardType = KeyboardType.Phone,
          imeAction = ImeAction.Done,
          onImeAction = { submit() },
        )
      }
    }

    Spacer(modifier = Modifier.height(32.dp))
    SubmitButton(text = "Send OTP", isSubmitting = false, onClick = { submit() })
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
  // Regenerated on "Resend", so a stale code stops working the moment a new one is issued.
  var generation by remember { mutableIntStateOf(0) }
  val expectedCode = remember(generation) { generateDemoOtp() }
  var digits by remember { mutableStateOf(List(Validators.OTP_LENGTH) { "" }) }
  var error by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }
  var secondsRemaining by remember(generation) { mutableIntStateOf(RESEND_COOLDOWN_SECONDS) }

  val focusRequesters = remember { List(Validators.OTP_LENGTH) { FocusRequester() } }
  val focusManager = LocalFocusManager.current
  val scope = rememberCoroutineScope()

  LaunchedEffect(generation) {
    while (secondsRemaining > 0) {
      delay(1000)
      secondsRemaining--
    }
  }

  fun verify() {
    if (isSubmitting) return
    val entered = digits.joinToString("")
    val validation = Validators.validateOtp(entered)
    if (validation != null) {
      error = validation.message("Code")
      return
    }
    if (entered != expectedCode) {
      error = FormError.OTP_MISMATCH.message()
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

  Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
    BackButton(onBack)
    Spacer(modifier = Modifier.height(24.dp))
    Text("Verify Your Mobile Number", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      "Enter the ${Validators.OTP_LENGTH}-digit code for ${formatIndianMobile(mobile)}.",
      fontSize = 16.sp,
      color = FixTheme.colors.textSecondary,
    )

    Spacer(modifier = Modifier.height(24.dp))
    DemoModeNotice("No SMS is sent in this build. Your code is $expectedCode.")

    Spacer(modifier = Modifier.height(32.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      digits.forEachIndexed { index, value ->
        OutlinedTextField(
          value = value,
          onValueChange = { raw ->
            error = null
            val entered = raw.filter { it.isDigit() }
            when {
              // Pasting the whole code into any one box fills the row rather than being truncated.
              entered.length > 1 -> {
                val spread = entered.take(Validators.OTP_LENGTH)
                digits = List(Validators.OTP_LENGTH) { i -> spread.getOrNull(i)?.toString() ?: "" }
                focusManager.clearFocus()
              }
              entered.isEmpty() -> {
                digits = digits.toMutableList().also { it[index] = "" }
              }
              else -> {
                digits = digits.toMutableList().also { it[index] = entered }
                if (index < Validators.OTP_LENGTH - 1) {
                  focusRequesters[index + 1].requestFocus()
                } else {
                  focusManager.clearFocus()
                }
              }
            }
          },
          modifier = Modifier.weight(1f).height(60.dp).focusRequester(focusRequesters[index]),
          enabled = !isSubmitting,
          singleLine = true,
          keyboardOptions =
            KeyboardOptions(
              keyboardType = KeyboardType.NumberPassword,
              imeAction =
                if (index == Validators.OTP_LENGTH - 1) ImeAction.Done else ImeAction.Next,
            ),
          textStyle =
            LocalTextStyle.current.copy(
              textAlign = TextAlign.Center,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
            ),
          isError = error != null,
          shape = RoundedCornerShape(8.dp),
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedBorderColor = FixTheme.colors.primary,
              unfocusedBorderColor = FixTheme.colors.border,
            ),
        )
      }
    }

    if (error != null) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
    }

    Spacer(modifier = Modifier.height(24.dp))
    Text(
      text =
        if (secondsRemaining > 0) "Resend code in ${secondsRemaining}s" else "Resend code",
      color = if (secondsRemaining > 0) FixTheme.colors.textSecondary else FixTheme.colors.primary,
      fontWeight = FontWeight.SemiBold,
      modifier =
        Modifier.clickable(enabled = secondsRemaining == 0 && !isSubmitting) {
          digits = List(Validators.OTP_LENGTH) { "" }
          error = null
          generation++
        },
    )

    Spacer(modifier = Modifier.height(32.dp))
    SubmitButton(text = "Verify", isSubmitting = isSubmitting, onClick = { verify() })

    Spacer(modifier = Modifier.height(16.dp))
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      TextButton(onClick = onBack, enabled = !isSubmitting) {
        Text("Edit mobile number", color = FixTheme.colors.textSecondary, fontWeight = FontWeight.Medium)
      }
    }
  }

  LaunchedEffect(Unit) { focusRequesters.first().requestFocus() }
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
  val scope = rememberCoroutineScope()

  fun submitMobile() {
    val validation = Validators.validateMobile(mobile)
    if (validation != null) {
      mobileError = validation.message("Mobile number")
      return
    }
    isSubmitting = true
    scope.launch {
      if (viewModel.accountExists(mobile)) {
        mobileError = null
        generation++
        step = 2
      } else {
        mobileError = FormError.NO_ACCOUNT_FOR_MOBILE.message()
      }
      isSubmitting = false
    }
  }

  fun submitOtp() {
    val validation = Validators.validateOtp(otp)
    otpError =
      when {
        validation != null -> validation.message("Code")
        otp.filter { it.isDigit() } != expectedCode -> FormError.OTP_MISMATCH.message()
        else -> null
      }
    if (otpError == null) step = 3
  }

  fun submitNewPassword() {
    val passwordValidation = Validators.validatePassword(newPassword)
    val confirmValidation =
      Validators.validatePasswordConfirmation(newPassword, confirmPassword)
    newPasswordError = passwordValidation?.message("Password")
    confirmPasswordError = confirmValidation?.message("Confirmation")
    if (passwordValidation != null || confirmValidation != null) return

    isSubmitting = true
    focusManager.clearFocus()
    scope.launch {
      val reset = viewModel.resetPassword(mobile, newPassword)
      isSubmitting = false
      if (reset) onPasswordReset() else newPasswordError = FormError.UNEXPECTED.message()
    }
  }

  Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
    BackButton(onBack)
    Spacer(modifier = Modifier.height(24.dp))

    when (step) {
      1 -> {
        Text("Forgot Password", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          "Enter your registered mobile number and we'll send a verification code.",
          fontSize = 16.sp,
          color = FixTheme.colors.textSecondary,
        )
        Spacer(modifier = Modifier.height(32.dp))
        AuthTextField(
          value = mobile,
          onValueChange = {
            mobile = it
            mobileError = null
          },
          label = "Mobile Number",
          leadingIcon = Icons.Default.Phone,
          error = mobileError,
          keyboardType = KeyboardType.Phone,
          imeAction = ImeAction.Done,
          onImeAction = { submitMobile() },
          enabled = !isSubmitting,
        )
        Spacer(modifier = Modifier.height(32.dp))
        SubmitButton(text = "Send OTP", isSubmitting = isSubmitting, onClick = { submitMobile() })
      }
      2 -> {
        Text("Verify OTP", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Code sent to ${formatIndianMobile(normalizeMobile(mobile))}.", fontSize = 16.sp, color = FixTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(24.dp))
        DemoModeNotice("No SMS is sent in this build. Your code is $expectedCode.")
        Spacer(modifier = Modifier.height(24.dp))
        AuthTextField(
          value = otp,
          onValueChange = {
            otp = it.filter { c -> c.isDigit() }.take(Validators.OTP_LENGTH)
            otpError = null
          },
          label = "Enter OTP",
          leadingIcon = Icons.Default.Lock,
          error = otpError,
          keyboardType = KeyboardType.NumberPassword,
          imeAction = ImeAction.Done,
          onImeAction = { submitOtp() },
        )
        Spacer(modifier = Modifier.height(32.dp))
        SubmitButton(text = "Verify", isSubmitting = false, onClick = { submitOtp() })
      }
      else -> {
        Text("Reset Password", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = FixTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Choose a new password for your account.", fontSize = 16.sp, color = FixTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(32.dp))
        AuthTextField(
          value = newPassword,
          onValueChange = {
            newPassword = it
            newPasswordError = null
          },
          label = "New Password",
          leadingIcon = Icons.Default.Lock,
          error = newPasswordError,
          supportingText =
            "At least ${Validators.MIN_PASSWORD_LENGTH} characters, with a letter and a number",
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
          label = "Confirm Password",
          leadingIcon = Icons.Default.Lock,
          error = confirmPasswordError,
          keyboardType = KeyboardType.Password,
          imeAction = ImeAction.Done,
          onImeAction = { submitNewPassword() },
          enabled = !isSubmitting,
          visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(32.dp))
        SubmitButton(
          text = "Save New Password",
          isSubmitting = isSubmitting,
          onClick = { submitNewPassword() },
        )
      }
    }
  }
}

// -------------------------------------------------------------------- shared pieces

@Composable
private fun BackButton(onBack: () -> Unit) {
  IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
  }
}

/**
 * States plainly that this build has no SMS gateway.
 *
 * Showing "OTP sent to your mobile" when nothing was sent would leave the user waiting for a
 * message that is never coming.
 */
@Composable
private fun DemoModeNotice(message: String) {
  Surface(
    color = FixTheme.colors.primary.copy(alpha = 0.08f),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(
        Icons.Outlined.Info,
        contentDescription = null,
        tint = FixTheme.colors.primary,
        modifier = Modifier.size(20.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text("Demo mode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = FixTheme.colors.textPrimary)
        Text(message, fontSize = 13.sp, color = FixTheme.colors.textSecondary)
      }
    }
  }
}

private fun generateDemoOtp(): String =
  (1..Validators.OTP_LENGTH).map { Random.nextInt(0, 10) }.joinToString("")
