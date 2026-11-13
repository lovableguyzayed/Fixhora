package com.example.ui.screens.auth

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.auth.AuthError
import com.example.data.auth.AuthException
import com.example.data.auth.AuthRepository
import com.example.data.auth.OtpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val phone: String = "",
    val verificationId: String? = null,
    val sending: Boolean = false,
    val verifying: Boolean = false,
    val googleBusy: Boolean = false,
    val error: AuthError? = null,
    /** Set when Android read the OTP from the SMS by itself. */
    val autoFilledCode: String? = null,
    val wrongTries: Int = 0,
    /** Bumped each time a new OTP is sent, so the screen restarts its countdown. */
    val codeSentCount: Int = 0,
    val signedIn: Boolean = false,
) {
    val triesLeft get() = (MAX_WRONG_TRIES - wrongTries).coerceAtLeast(0)
    companion object { const val MAX_WRONG_TRIES = 3 }
}

/**
 * Login flow state. Phone number and verification id are kept in [SavedStateHandle], so the
 * OTP screen still works if a low-RAM phone closes the app while the user reads the SMS.
 */
class AuthViewModel(
    private val auth: AuthRepository,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val _ui = MutableStateFlow(
        AuthUiState(
            phone = saved["phone"] ?: "",
            verificationId = saved["verificationId"],
            codeSentCount = saved["codeSentCount"] ?: 0,
        )
    )
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()
    val isTestMode get() = auth.isTestMode

    fun sendOtp(activity: Activity, phone10: String, resend: Boolean = false, onCodeSent: () -> Unit = {}) {
        if (_ui.value.sending) return
        saved["phone"] = phone10
        _ui.update { it.copy(phone = phone10, sending = true, error = null, autoFilledCode = null) }
        auth.sendOtp(activity, phone10, resend) { result ->
            when (result) {
                is OtpResult.CodeSent -> {
                    val count = _ui.value.codeSentCount + 1
                    saved["verificationId"] = result.verificationId
                    saved["codeSentCount"] = count
                    _ui.update {
                        it.copy(sending = false, verificationId = result.verificationId, wrongTries = 0, codeSentCount = count)
                    }
                    onCodeSent()
                }
                is OtpResult.AutoVerified -> _ui.update {
                    it.copy(sending = false, autoFilledCode = result.code, signedIn = true)
                }
                is OtpResult.Failed -> _ui.update { it.copy(sending = false, error = result.error) }
            }
        }
    }

    fun verify(code: String) {
        val s = _ui.value
        val id = s.verificationId ?: return
        if (s.verifying || code.length != 6 || s.triesLeft == 0) return
        _ui.update { it.copy(verifying = true, error = null) }
        viewModelScope.launch {
            auth.verifyOtp(id, code)
                .onSuccess { _ui.update { it.copy(verifying = false, signedIn = true) } }
                .onFailure { e ->
                    val err = (e as? AuthException)?.error ?: AuthError.Unknown
                    _ui.update {
                        it.copy(
                            verifying = false,
                            error = err,
                            wrongTries = if (err == AuthError.WrongCode) it.wrongTries + 1 else it.wrongTries,
                        )
                    }
                }
        }
    }

    fun signInWithGoogle(activity: Activity) {
        if (_ui.value.googleBusy) return
        _ui.update { it.copy(googleBusy = true, error = null) }
        viewModelScope.launch {
            auth.signInWithGoogle(activity)
                .onSuccess { _ui.update { it.copy(googleBusy = false, signedIn = true) } }
                .onFailure { e ->
                    val err = (e as? AuthException)?.error ?: AuthError.Unknown
                    // Closing the Google popup is not an error worth showing.
                    _ui.update { it.copy(googleBusy = false, error = err.takeIf { it != AuthError.Cancelled }) }
                }
        }
    }

    fun clearError() = _ui.update { it.copy(error = null) }

    /** Called once the app has moved on after login, so a later visit starts fresh. */
    fun consumeSignedIn() = _ui.update { it.copy(signedIn = false) }

    companion object {
        fun factory(auth: AuthRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                AuthViewModel(auth, extras.createSavedStateHandle()) as T
        }
    }
}
