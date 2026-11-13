package com.example.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.AuthError
import com.example.ui.theme.*
import com.example.util.IndiaFormat
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.delay

val ErrorRed = Color(0xFFC62828)
val SuccessGreen = Color(0xFF2E7D32)

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Plain-language message for every login problem (null = show nothing). */
@Composable
fun authErrorText(error: AuthError?, triesLeft: Int = 0): String? = when (error) {
    null, AuthError.Cancelled -> null
    AuthError.InvalidNumber -> stringResource(R.string.auth_err_invalid_number)
    AuthError.TooManyRequests -> stringResource(R.string.auth_err_too_many)
    AuthError.Network -> stringResource(R.string.auth_err_network)
    AuthError.WrongCode -> if (triesLeft > 0) pluralStringResource(R.plurals.auth_err_wrong_code, triesLeft, triesLeft)
        else stringResource(R.string.auth_err_wrong_code_no_tries)
    AuthError.CodeExpired -> stringResource(R.string.auth_err_code_expired)
    AuthError.NoGoogleAccount -> stringResource(R.string.auth_err_no_google)
    AuthError.NotConfigured -> stringResource(R.string.auth_err_google_not_ready)
    AuthError.RecentLoginRequired -> stringResource(R.string.auth_err_recent_login)
    AuthError.Unknown -> stringResource(R.string.auth_err_unknown)
}

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
internal fun AuthTitle(title: String, subtitle: String) {
    Text(title, fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
    Spacer(Modifier.height(8.dp))
    Text(subtitle, fontSize = 15.sp, lineHeight = 22.sp, color = SecondaryGrey)
}

@Composable
internal fun PrimaryButton(text: String, enabled: Boolean = true, busy: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled && !busy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
    ) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
        } else {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun TestModeNotice() {
    Surface(color = Color(0xFFFFF3E0), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.auth_test_mode_notice),
            color = Color(0xFF8A4B00), fontSize = 13.sp, lineHeight = 18.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun MobileLoginScreen(
    ui: AuthUiState,
    isTestMode: Boolean,
    onSendOtp: (Activity, String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var mobile by rememberSaveable { mutableStateOf(ui.phone) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var hintUnavailable by rememberSaveable { mutableStateOf(false) }
    val isValid = IndiaFormat.isValidMobile(mobile)
    val submit = {
        val activity = context.findActivity()
        if (isValid && activity != null) onSendOtp(activity, mobile) else showError = true
    }

    // Google's "Phone Number Hint": the user picks their SIM number in one tap. No permission needed.
    val hintLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            runCatching { Identity.getSignInClient(context).getPhoneNumberFromIntent(res.data) }
                .onSuccess { mobile = IndiaFormat.cleanMobile(it); showError = false }
        }
    }
    val askSimNumber = {
        val activity = context.findActivity()
        if (activity == null) hintUnavailable = true
        else Identity.getSignInClient(activity)
            .getPhoneNumberHintIntent(GetPhoneNumberHintIntentRequest.builder().build())
            .addOnSuccessListener { pi -> hintLauncher.launch(IntentSenderRequest.Builder(pi.intentSender).build()) }
            .addOnFailureListener { hintUnavailable = true }
        Unit
    }

    AuthScreenColumn {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.mobilelogin_back))
        }
        Spacer(Modifier.height(16.dp))
        AuthTitle(stringResource(R.string.auth_mobile_title), stringResource(R.string.auth_mobile_subtitle))
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = {
                mobile = IndiaFormat.cleanMobile(it)
                if (IndiaFormat.isValidMobile(mobile)) showError = false
            },
            label = { Text(stringResource(R.string.mobilelogin_mobile_number)) },
            leadingIcon = {
                Text("+91", color = DarkNavy, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp))
            },
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
            isError = showError && !isValid,
            supportingText = {
                if (showError && !isValid) {
                    Text(
                        if (mobile.length == 10) stringResource(R.string.mobile_error_start_digit)
                        else stringResource(R.string.mobile_error_ten_digits)
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary, unfocusedBorderColor = BorderGrey)
        )

        if (!hintUnavailable && mobile.isEmpty()) {
            AssistChip(
                onClick = askSimNumber,
                label = { Text(stringResource(R.string.auth_pick_sim_number), fontWeight = FontWeight.SemiBold) },
                leadingIcon = { Icon(Icons.Default.SimCard, contentDescription = null, tint = BluePrimary) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFEEF4FF), labelColor = BluePrimary),
                border = null,
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }

        authErrorText(ui.error)?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = ErrorRed, fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(stringResource(R.string.auth_send_otp), busy = ui.sending, onClick = submit)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.auth_no_password_needed),
            color = SecondaryGrey, fontSize = 13.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (isTestMode) { Spacer(Modifier.height(16.dp)); TestModeNotice() }
    }
}

private const val OTP_LENGTH = 6
private const val RESEND_SECONDS = 30

@Composable
fun OtpVerificationScreen(
    ui: AuthUiState,
    isTestMode: Boolean,
    onVerify: (String) -> Unit,
    onResend: (Activity) -> Unit,
    onChangeNumber: () -> Unit,
    onEdited: () -> Unit = {},
) {
    val context = LocalContext.current
    var otp by rememberSaveable { mutableStateOf("") }
    // Restart the countdown each time a new OTP is sent (survives rotation/app restart).
    var timer by rememberSaveable(ui.codeSentCount) { mutableStateOf(RESEND_SECONDS) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(timer) {
        if (timer > 0) { delay(1000); timer-- }
    }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    LaunchedEffect(ui.autoFilledCode) { ui.autoFilledCode?.let { otp = it } }
    LaunchedEffect(ui.codeSentCount) { otp = "" }
    // Check automatically as soon as 6 digits are typed or pasted.
    LaunchedEffect(otp) { if (otp.length == OTP_LENGTH && ui.autoFilledCode == null) onVerify(otp) }

    val errorText = authErrorText(ui.error, ui.triesLeft)
    val boxColor = { index: Int ->
        when {
            errorText != null && ui.error == AuthError.WrongCode -> ErrorRed
            index <= otp.length -> BluePrimary
            else -> BorderGrey
        }
    }

    AuthScreenColumn {
        IconButton(onClick = onChangeNumber) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.mobilelogin_back))
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.auth_otp_title), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.auth_otp_sent_to, IndiaFormat.displayMobile(ui.phone)),
            fontSize = 15.sp, lineHeight = 22.sp, color = SecondaryGrey
        )
        TextButton(onClick = onChangeNumber, contentPadding = PaddingValues(0.dp)) {
            Text(stringResource(R.string.auth_change_number), color = BluePrimary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))

        val otpDescription = stringResource(R.string.mobilelogin_6_digit_otp)
        BasicTextField(
            value = otp,
            onValueChange = { otp = it.filter(Char::isDigit).take(OTP_LENGTH); onEdited() },
            enabled = !ui.verifying && ui.triesLeft > 0,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (otp.length == OTP_LENGTH) onVerify(otp) }),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = TextStyle(color = Color.Transparent),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).semantics { contentDescription = otpDescription },
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(OTP_LENGTH) { index ->
                        Box(
                            modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                                .border(if (index == otp.length) 2.dp else 1.dp, boxColor(index), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(otp.getOrNull(index)?.toString() ?: "", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                        }
                    }
                }
            }
        )

        Spacer(Modifier.height(12.dp))
        when {
            ui.autoFilledCode != null || (ui.signedIn && otp.length == OTP_LENGTH) -> StatusLine(
                Icons.Default.CheckCircle, stringResource(R.string.auth_otp_auto_filled), SuccessGreen
            )
            errorText != null -> StatusLine(Icons.Default.Warning, errorText, ErrorRed)
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.auth_didnt_get_it), color = SecondaryGrey, fontSize = 14.sp)
            TextButton(
                onClick = { context.findActivity()?.let { otp = ""; onResend(it) } },
                enabled = timer == 0 && !ui.sending
            ) {
                Text(
                    if (timer > 0) stringResource(R.string.otp_resend_in, timer) else stringResource(R.string.otp_resend),
                    fontWeight = FontWeight.SemiBold,
                    color = if (timer > 0) SecondaryGrey else BluePrimary
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            stringResource(R.string.auth_verify),
            enabled = otp.length == OTP_LENGTH && ui.triesLeft > 0,
            busy = ui.verifying,
            onClick = { onVerify(otp) }
        )
        if (isTestMode) { Spacer(Modifier.height(16.dp)); TestModeNotice() }
    }
}

@Composable
private fun StatusLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** After first login: ask only for a name. A single name (no surname) is fine. */
@Composable
fun NameScreen(initialName: String = "", saving: Boolean, onSave: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var showError by rememberSaveable { mutableStateOf(false) }
    val clean = name.trim().replace(Regex("\\s+"), " ")
    val submit = { if (clean.length >= 2) onSave(clean) else showError = true }

    AuthScreenColumn {
        Spacer(Modifier.height(24.dp))
        AuthTitle(stringResource(R.string.auth_name_title), stringResource(R.string.auth_name_subtitle))
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(40); if (it.trim().length >= 2) showError = false },
            label = { Text(stringResource(R.string.auth_your_name)) },
            isError = showError,
            supportingText = {
                Text(if (showError) stringResource(R.string.auth_name_error) else stringResource(R.string.auth_name_hint))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BluePrimary, unfocusedBorderColor = BorderGrey)
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(stringResource(R.string.auth_continue), busy = saving, onClick = submit)
    }
}
