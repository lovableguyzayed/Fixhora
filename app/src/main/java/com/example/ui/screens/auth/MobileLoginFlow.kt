package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.ui.theme.*
import com.example.util.IndiaFormat
import kotlinx.coroutines.delay

/** Shared layout for the simple auth screens: safe areas, scrolling and keyboard padding. */
@Composable
internal fun AuthScreenColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun MobileLoginScreen(onSendOtp: (String) -> Unit, onBack: () -> Unit) {
    var mobile by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    val isValid = IndiaFormat.isValidMobile(mobile)
    val submit = { if (isValid) onSendOtp(mobile) else showError = true }

    AuthScreenColumn {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Login with Mobile Number",
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your mobile number to receive a verification code.",
            fontSize = 16.sp,
            color = SecondaryGrey
        )
        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = {
                mobile = IndiaFormat.cleanMobile(it)
                if (IndiaFormat.isValidMobile(mobile)) showError = false
            },
            label = { Text("Mobile number") },
            leadingIcon = { Text("+91", color = DarkNavy, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp, end = 4.dp)) },
            isError = showError && !isValid,
            supportingText = {
                if (showError && !isValid) {
                    Text(
                        if (mobile.length == 10) "Mobile number should start with 6, 7, 8 or 9"
                        else "Enter your 10-digit mobile number"
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = BorderGrey
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = submit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text("Send OTP", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private const val OTP_LENGTH = 6
private const val RESEND_SECONDS = 30

@Composable
fun OtpVerificationScreen(onVerified: () -> Unit, onBack: () -> Unit, mobile: String = "") {
    var otp by rememberSaveable { mutableStateOf("") }
    var timer by rememberSaveable { mutableStateOf(RESEND_SECONDS) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000)
            timer--
        }
    }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    AuthScreenColumn {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Verify Your Mobile Number",
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (mobile.isNotEmpty()) "Enter the 6-digit OTP sent to ${IndiaFormat.displayMobile(mobile)}"
            else "Enter the 6-digit OTP sent to your mobile.",
            fontSize = 16.sp,
            color = SecondaryGrey
        )
        Spacer(modifier = Modifier.height(32.dp))

        // One real text field drawn as 6 boxes: typing, Backspace, paste and keyboard
        // OTP suggestions all work, and the boxes shrink to fit small phones.
        BasicTextField(
            value = otp,
            onValueChange = { otp = it.filter(Char::isDigit).take(OTP_LENGTH) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (otp.length == OTP_LENGTH) onVerified() }),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = TextStyle(color = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .semantics { contentDescription = "6-digit OTP" },
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(OTP_LENGTH) { index ->
                        val isNext = index == otp.length
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 56.dp)
                                .border(
                                    width = if (isNext) 2.dp else 1.dp,
                                    color = if (isNext || index < otp.length) BluePrimary else BorderGrey,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = otp.getOrNull(index)?.toString() ?: "",
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

        Spacer(modifier = Modifier.height(24.dp))
        TextButton(
            onClick = { timer = RESEND_SECONDS; otp = "" },
            enabled = timer == 0,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = if (timer > 0) "Resend OTP in ${timer}s" else "Resend OTP",
                color = if (timer > 0) SecondaryGrey else BluePrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onVerified,
            enabled = otp.length == OTP_LENGTH,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text("Verify", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Change mobile number", color = SecondaryGrey, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onPasswordReset: () -> Unit, onBack: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var step by rememberSaveable { mutableStateOf(1) } // 1 = Request, 2 = Verify, 3 = Reset

    AuthScreenColumn {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        if (step == 1) {
            Text(
                text = "Forgot Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your registered Mobile Number or Email to receive an OTP.",
                fontSize = 16.sp,
                color = SecondaryGrey
            )
            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Mobile or Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = { step = 2 },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Send OTP", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        } else if (step == 2) {
            Text(
                text = "Verify OTP",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(40.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                label = { Text("Enter OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = { step = 3 },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Verify", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
             Text(
                text = "Reset Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(40.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onPasswordReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Confirm", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
