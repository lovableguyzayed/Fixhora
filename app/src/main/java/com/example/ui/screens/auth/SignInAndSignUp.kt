package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.ui.components.ErrorText
import com.example.ui.components.FixhoraTextField
import com.example.ui.components.GoogleIcon
import com.example.ui.components.OrDivider
import com.example.ui.components.PasswordTextField
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ScreenHeader
import com.example.ui.components.SocialButton
import com.example.ui.components.Spacing
import com.example.ui.theme.*

@Composable
fun SignInScreen(
    onLoginSuccess: () -> Unit,
    onMobileLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onBack: () -> Unit
) {
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var keepSignedIn by remember { mutableStateOf(true) }
    var submitted by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val mobileError = when {
        !submitted -> null
        mobile.isEmpty() -> "Enter your mobile number"
        !isValidIndianMobile(mobile) -> "Enter a valid 10-digit mobile number"
        else -> null
    }
    val passwordError = if (submitted && password.isEmpty()) "Enter your password" else null

    fun attemptSignIn() {
        submitted = true
        focusManager.clearFocus()
        if (isValidIndianMobile(mobile) && password.isNotEmpty()) onLoginSuccess()
    }

    AuthScreenLayout(
        onBack = onBack,
        footer = {
            AuthSwitchPrompt(
                prompt = "Don't have an account?",
                action = "Create account",
                onClick = onCreateAccountClick
            )
        }
    ) {
        ScreenHeader(title = "Welcome back", subtitle = "Sign in to continue to FixoraX")
        Spacer(modifier = Modifier.height(Spacing.xl))

        FixhoraTextField(
            value = mobile,
            onValueChange = { mobile = it.digitsOnly(10) },
            label = "Mobile number",
            leadingIcon = Icons.Default.Phone,
            prefix = "+91 ",
            errorText = mobileError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            leadingIcon = Icons.Default.Lock,
            errorText = passwordError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { attemptSignIn() })
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabeledCheckbox(
                checked = keepSignedIn,
                onCheckedChange = { keepSignedIn = it },
                text = "Keep me signed in"
            )
            TextButton(
                onClick = onForgotPasswordClick,
                contentPadding = PaddingValues(start = Spacing.sm, end = 0.dp)
            ) {
                Text("Forgot password?", color = BluePrimary, style = MaterialTheme.typography.titleSmall)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        PrimaryButton(text = "Sign In", onClick = { attemptSignIn() })

        Spacer(modifier = Modifier.height(Spacing.lg))
        OrDivider()
        Spacer(modifier = Modifier.height(Spacing.lg))

        SocialButton(text = "Continue with Mobile OTP", onClick = onMobileLoginClick) {
            Icon(Icons.Default.Smartphone, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        SocialButton(
            text = "Continue with Google",
            onClick = { Toast.makeText(context, "Google sign-in is coming soon", Toast.LENGTH_SHORT).show() }
        ) {
            Icon(GoogleIcon, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        SocialButton(
            text = "Continue with Email",
            onClick = { Toast.makeText(context, "Email sign-in is coming soon", Toast.LENGTH_SHORT).show() }
        ) {
            Icon(Icons.Default.Email, contentDescription = null, tint = SecondaryGrey, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun CreateAccountScreen(
    onCreateSuccess: () -> Unit,
    onMobileOtpClick: () -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreed by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val nameError = if (submitted && fullName.isBlank()) "Enter your full name" else null
    val mobileError = when {
        !submitted -> null
        mobile.isEmpty() -> "Enter your mobile number"
        !isValidIndianMobile(mobile) -> "Enter a valid 10-digit mobile number"
        else -> null
    }
    val emailError = if (submitted && email.isNotBlank() && !isValidEmail(email)) "Enter a valid email address" else null
    val passwordError = if (submitted && password.length < 8) "Use at least 8 characters" else null
    val confirmError = if (submitted && confirmPassword != password) "Passwords do not match" else null
    val termsError = submitted && !agreed

    fun attemptCreate() {
        submitted = true
        focusManager.clearFocus()
        val valid = fullName.isNotBlank() && isValidIndianMobile(mobile) &&
            (email.isBlank() || isValidEmail(email)) && password.length >= 8 &&
            confirmPassword == password && agreed
        if (valid) onCreateSuccess()
    }

    AuthScreenLayout(
        onBack = onBack,
        footer = {
            AuthSwitchPrompt(prompt = "Already have an account?", action = "Sign in", onClick = onBack)
        }
    ) {
        ScreenHeader(title = "Create your account", subtitle = "It only takes a minute to get started.")
        Spacer(modifier = Modifier.height(Spacing.xl))

        val nextAction = KeyboardOptions(imeAction = ImeAction.Next)
        FixhoraTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = "Full name",
            leadingIcon = Icons.Default.Person,
            errorText = nameError,
            keyboardOptions = nextAction
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        FixhoraTextField(
            value = mobile,
            onValueChange = { mobile = it.digitsOnly(10) },
            label = "Mobile number",
            leadingIcon = Icons.Default.Phone,
            prefix = "+91 ",
            errorText = mobileError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        FixhoraTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = "Email address (optional)",
            leadingIcon = Icons.Default.Email,
            errorText = emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { attemptCreate() })
        )

        Spacer(modifier = Modifier.height(Spacing.xs))
        LabeledCheckbox(
            checked = agreed,
            onCheckedChange = { agreed = it },
            text = buildAnnotatedString {
                append("I agree to the ")
                withStyle(SpanStyle(color = BluePrimary, fontWeight = FontWeight.SemiBold)) { append("Terms") }
                append(" & ")
                withStyle(SpanStyle(color = BluePrimary, fontWeight = FontWeight.SemiBold)) { append("Privacy Policy") }
            },
            isError = termsError
        )
        if (termsError) {
            ErrorText("Please accept the terms to continue")
        }

        Spacer(modifier = Modifier.height(Spacing.md))
        PrimaryButton(text = "Create Account", onClick = { attemptCreate() })

        Spacer(modifier = Modifier.height(Spacing.lg))
        OrDivider()
        Spacer(modifier = Modifier.height(Spacing.lg))

        SocialButton(text = "Sign up with Mobile OTP", onClick = onMobileOtpClick) {
            Icon(Icons.Default.Smartphone, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(Spacing.sm))
        SocialButton(
            text = "Sign up with Google",
            onClick = { Toast.makeText(context, "Google sign-up is coming soon", Toast.LENGTH_SHORT).show() }
        ) {
            Icon(GoogleIcon, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(20.dp))
        }
    }
}

/** "Prompt  Action" line at the bottom of auth screens, with a proper 48dp touch target. */
@Composable
fun AuthSwitchPrompt(prompt: String, action: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Text(prompt, style = MaterialTheme.typography.bodyMedium, color = SecondaryGrey)
        TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = Spacing.xs)) {
            Text(action, style = MaterialTheme.typography.titleSmall, color = BluePrimary)
        }
    }
}

@Composable
fun LabeledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) = LabeledCheckbox(checked, onCheckedChange, buildAnnotatedString { append(text) }, modifier)

/** Checkbox whose whole row (box + label) is one toggle target. */
@Composable
fun LabeledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: androidx.compose.ui.text.AnnotatedString,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Row(
        modifier = modifier
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .heightIn(min = 48.dp)
            .padding(end = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(end = Spacing.xs),
            colors = CheckboxDefaults.colors(
                checkedColor = BluePrimary,
                uncheckedColor = if (isError) ErrorRed else HintGrey
            )
        )
        Text(text, style = MaterialTheme.typography.bodyMedium, color = DarkNavy)
    }
}
