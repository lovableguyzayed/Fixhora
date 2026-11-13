package com.example.data.auth

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/** The signed-in person. [name] is null until they tell us what to call them. */
data class AppUser(
    val uid: String,
    val phone: String?,
    val email: String?,
    val name: String?,
)

/** Every login problem the screens can explain in plain words. */
enum class AuthError {
    InvalidNumber, TooManyRequests, Network, WrongCode, CodeExpired,
    Cancelled, NoGoogleAccount, NotConfigured, RecentLoginRequired, Unknown
}

class AuthException(val error: AuthError, cause: Throwable? = null) : Exception(error.name, cause)

/** What happens after asking for an OTP. */
sealed interface OtpResult {
    /** SMS sent; the user types the code (or Android reads it). */
    data class CodeSent(val verificationId: String) : OtpResult
    /** Android read the SMS by itself and the user is already signed in. */
    data class AutoVerified(val user: AppUser, val code: String?) : OtpResult
    data class Failed(val error: AuthError) : OtpResult
}

interface AuthRepository {
    /** True when Firebase is not set up yet: OTP is always 123456 and nothing leaves the phone. */
    val isTestMode: Boolean
    val currentUser: StateFlow<AppUser?>

    /** [phone10] is a cleaned 10-digit Indian mobile number. */
    fun sendOtp(activity: Activity, phone10: String, resend: Boolean, onResult: (OtpResult) -> Unit)
    suspend fun verifyOtp(verificationId: String, code: String): Result<AppUser>
    suspend fun signInWithGoogle(activity: Activity): Result<AppUser>
    suspend fun updateName(name: String): Result<AppUser>
    suspend fun signOut()
    /** Deletes the login account itself. Local data is wiped separately by the caller. */
    suspend fun deleteAccount(): Result<Unit>
}
