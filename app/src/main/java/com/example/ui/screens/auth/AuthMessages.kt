package com.example.ui.screens.auth

import android.content.Context
import androidx.annotation.StringRes
import com.example.R
import com.example.util.FieldError
import com.example.util.Validators

/**
 * Turns validation codes into text a person can act on.
 *
 * Every message says what is wrong *and* what to do about it — "Enter a 10-digit mobile number"
 * rather than "Invalid input". Keeping the mapping here, away from the rules themselves, is what
 * lets the Hindi build swap these strings without touching validation.
 *
 * These take a [Context] rather than being `@Composable`, because several call sites are local
 * functions that resolve a message inside `scope.launch { }` — outside composition, where a
 * composable call will not compile. The context handed in comes from `LocalContext`, which
 * `LocalizedContent` has already switched to the chosen language, so resolving through it honours
 * the preference exactly as `stringResource` would.
 *
 * [fieldLabel] is a resource id rather than a string so the caller cannot accidentally pass an
 * untranslated literal into a translated sentence.
 */
fun FieldError.message(context: Context, @StringRes fieldLabel: Int): String =
  when (this) {
    FieldError.REQUIRED -> context.getString(R.string.error_required, context.getString(fieldLabel))
    FieldError.NAME_TOO_SHORT -> context.getString(R.string.error_name_too_short)
    FieldError.MOBILE_INCOMPLETE -> context.getString(R.string.error_mobile_incomplete)
    FieldError.MOBILE_INVALID_PREFIX -> context.getString(R.string.error_mobile_invalid_prefix)
    FieldError.EMAIL_INVALID -> context.getString(R.string.error_email_invalid)
    FieldError.PASSWORD_TOO_SHORT ->
      context.getString(R.string.error_password_too_short, Validators.MIN_PASSWORD_LENGTH)
    FieldError.PASSWORD_NEEDS_LETTER_AND_DIGIT ->
      context.getString(R.string.error_password_needs_letter_and_digit)
    FieldError.PASSWORD_MISMATCH -> context.getString(R.string.error_password_mismatch)
    FieldError.OTP_INCOMPLETE ->
      context.getString(R.string.error_otp_incomplete, Validators.OTP_LENGTH)
    FieldError.PIN_CODE_INVALID -> context.getString(R.string.error_pin_code_invalid)
  }

fun FormError.message(context: Context): String =
  when (this) {
    FormError.INVALID_CREDENTIALS -> context.getString(R.string.error_invalid_credentials)
    FormError.MOBILE_ALREADY_REGISTERED -> context.getString(R.string.error_mobile_taken)
    FormError.NO_ACCOUNT_FOR_MOBILE -> context.getString(R.string.error_no_account_for_mobile)
    FormError.OTP_MISMATCH -> context.getString(R.string.error_otp_mismatch)
    FormError.UNEXPECTED -> context.getString(R.string.error_unexpected)
  }
