package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FixhoraTextField
import com.example.ui.components.PasswordTextField
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.theme.*
import kotlinx.coroutines.delay

private const val OtpLength = 6
private const val ResendSeconds = 45

@Composable
fun MobileLoginScreen(onSendOtp: (String) -> Unit, onBack: () -> Unit) {
    var mobile by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val isValid = isValidIndianMobile(mobile)

    fun send() {
        submitted = true
        if (isValid) onSendOtp(mobile)
    }

    AuthScreenLayout(
        onBack = onBack,
        footer = {
            PrimaryButton(text = "Send OTP", onClick = { send() }, enabled = mobile.length == 10)
        }
    ) {
        ScreenHeader(
            title = "Log in with mobile",
            subtitle = "We'll send a 6-digit verification code to this number."
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        FixhoraTextField(
            value = mobile,
            onValueChange = { mobile = it.digitsOnly(10) },
            label = "Mobile number",
            leadingIcon = Icons.Default.Phone,
            prefix = "+91 ",
            errorText = if (submitted && !isValid) "Enter a valid 10-digit mobile number" else null,
            helperText = "Standard SMS charges may apply",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { send() })
        )
    }
}

@Composable
fun OtpVerificationScreen(phoneNumber: String, onVerified: () -> Unit, onBack: () -> Unit) {
    var otp by remember { mutableStateOf("") }

    AuthScreenLayout(
        onBack = onBack,
        footer = {
            PrimaryButton(text = "Verify", onClick = onVerified, enabled = otp.length == OtpLength)
        }
    ) {
        ScreenHeader(title = "Verify your number")
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = buildAnnotatedString {
                append("Enter the 6-digit code sent to ")
                withStyle(SpanStyle(color = DarkNavy, fontWeight = FontWeight.SemiBold)) {
                    append(formatPhoneForDisplay(phoneNumber))
                }
            },
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryGrey
        )
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Text("Change number", style = MaterialTheme.typography.titleSmall, color = BluePrimary)
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
        OtpInput(value = otp, onValueChange = { otp = it })
        Spacer(modifier = Modifier.height(Spacing.lg))
        ResendCodeRow(modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun ForgotPasswordScreen(onPasswordReset: () -> Unit, onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(1) } // 1 = Request, 2 = Verify, 3 = Reset
    var account by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val goBack: () -> Unit = {
        submitted = false
        if (step > 1) step -= 1 else onBack()
    }
    BackHandler(enabled = step > 1) { goBack() }

    val accountValid = isValidIndianMobile(account) || isValidEmail(account)
    val passwordError = if (submitted && newPassword.length < 8) "Use at least 8 characters" else null
    val confirmError = if (submitted && confirmPassword != newPassword) "Passwords do not match" else null

    AuthScreenLayout(
        onBack = goBack,
        footer = {
            when (step) {
                1 -> PrimaryButton(
                    text = "Send OTP",
                    enabled = account.isNotBlank(),
                    onClick = {
                        submitted = true
                        if (accountValid) {
                            submitted = false
                            focusManager.clearFocus()
                            step = 2
                        }
                    }
                )
                2 -> PrimaryButton(
                    text = "Verify",
                    enabled = otp.length == OtpLength,
                    onClick = { step = 3 }
                )
                else -> PrimaryButton(
                    text = "Reset Password",
                    enabled = newPassword.isNotEmpty() && confirmPassword.isNotEmpty(),
                    onClick = {
                        submitted = true
                        if (newPassword.length >= 8 && newPassword == confirmPassword) onPasswordReset()
                    }
                )
            }
        }
    ) {
        Text(
            text = "Step $step of 3",
            style = MaterialTheme.typography.labelMedium,
            color = BluePrimary
        )
        Spacer(modifier = Modifier.height(Spacing.xxs))
        when (step) {
            1 -> {
                ScreenHeader(
                    title = "Forgot password",
                    subtitle = "Enter your registered mobile number or email and we'll send you a code."
                )
                Spacer(modifier = Modifier.height(Spacing.xl))
                FixhoraTextField(
                    value = account,
                    onValueChange = { account = it.trim() },
                    label = "Mobile number or email",
                    leadingIcon = Icons.Default.Person,
                    errorText = if (submitted && !accountValid) "Enter a valid mobile number or email" else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
                )
            }
            2 -> {
                ScreenHeader(title = "Enter verification code")
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = buildAnnotatedString {
                        append("We sent a 6-digit code to ")
                        withStyle(SpanStyle(color = DarkNavy, fontWeight = FontWeight.SemiBold)) {
                            append(if (account.all { it.isDigit() }) formatPhoneForDisplay(account) else account)
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryGrey
                )
                Spacer(modifier = Modifier.height(Spacing.xl))
                OtpInput(value = otp, onValueChange = { otp = it })
                Spacer(modifier = Modifier.height(Spacing.lg))
                ResendCodeRow(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            else -> {
                ScreenHeader(
                    title = "Create new password",
                    subtitle = "Your new password must be different from the previous one."
                )
                Spacer(modifier = Modifier.height(Spacing.xl))
                PasswordTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "New password",
                    leadingIcon = Icons.Default.Lock,
                    errorText = passwordError,
                    helperText = "At least 8 characters",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm password",
                    leadingIcon = Icons.Default.Lock,
                    errorText = confirmError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                )
            }
        }
    }
}

/**
 * One hidden text field rendered as [length] boxes, so typing, pasting and backspace move
 * between digits naturally.
 */
@Composable
fun OtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = OtpLength,
) {
    val focusRequester = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = TextFieldValue(value, selection = TextRange(value.length)),
        onValueChange = { onValueChange(it.text.filter { c -> c.isDigit() }.take(length)) },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        singleLine = true,
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                repeat(length) { index ->
                    val char = value.getOrNull(index)?.toString() ?: ""
                    val isActive = focused && (index == value.length || (index == length - 1 && value.length == length))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = when {
                                    isActive -> BluePrimary
                                    char.isNotEmpty() -> DarkNavy.copy(alpha = 0.35f)
                                    else -> BorderGrey
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkNavy,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ResendCodeRow(modifier: Modifier = Modifier) {
    var secondsLeft by remember { mutableIntStateOf(ResendSeconds) }
    LaunchedEffect(secondsLeft) {
        if (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("Didn't receive the code?", style = MaterialTheme.typography.bodyMedium, color = SecondaryGrey)
        if (secondsLeft > 0) {
            Text(
                text = "  Resend in 0:%02d".format(secondsLeft),
                style = MaterialTheme.typography.titleSmall,
                color = HintGrey,
                modifier = Modifier.padding(vertical = 14.dp)
            )
        } else {
            TextButton(onClick = { secondsLeft = ResendSeconds }, contentPadding = PaddingValues(horizontal = Spacing.xs)) {
                Text("Resend code", style = MaterialTheme.typography.titleSmall, color = BluePrimary)
            }
        }
    }
}

private fun formatPhoneForDisplay(number: String): String =
    // Non-breaking spaces keep the number on one line.
    if (number.length == 10) "+91\u00A0${number.take(5)}\u00A0${number.drop(5)}" else "your mobile number"
