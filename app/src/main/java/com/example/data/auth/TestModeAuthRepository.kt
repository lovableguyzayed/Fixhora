package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Used until Firebase is configured (no google-services.json). No SMS is sent; the OTP is
 * always [TEST_OTP]. The "account" only lives on this phone. The OTP screen shows a clear
 * "Test mode" notice so this can never be mistaken for real login.
 */
class TestModeAuthRepository(context: Context) : AuthRepository {
    private val prefs = context.getSharedPreferences("test_mode_session", Context.MODE_PRIVATE)

    override val isTestMode = true
    private val user = MutableStateFlow(load())
    override val currentUser: StateFlow<AppUser?> = user.asStateFlow()

    private var pendingPhone: String? = null

    private fun load(): AppUser? {
        val uid = prefs.getString("uid", null) ?: return null
        return AppUser(uid, prefs.getString("phone", null), null, prefs.getString("name", null))
    }

    private fun save(u: AppUser?) {
        prefs.edit {
            clear()
            if (u != null) {
                putString("uid", u.uid); putString("phone", u.phone); putString("name", u.name)
            }
        }
        user.value = u
    }

    override fun sendOtp(activity: Activity, phone10: String, resend: Boolean, onResult: (OtpResult) -> Unit) {
        pendingPhone = phone10
        Handler(Looper.getMainLooper()).postDelayed({ onResult(OtpResult.CodeSent("test-$phone10")) }, 400)
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<AppUser> {
        if (code != TEST_OTP) return Result.failure(AuthException(AuthError.WrongCode))
        val phone = verificationId.removePrefix("test-")
        val existing = user.value?.takeIf { it.phone == "+91$phone" }
        val u = existing ?: AppUser(uid = "test-$phone", phone = "+91$phone", email = null,
            name = prefs.getString("name_$phone", null))
        save(u)
        return Result.success(u)
    }

    override suspend fun signInWithGoogle(activity: Activity): Result<AppUser> =
        Result.failure(AuthException(AuthError.NotConfigured))

    override suspend fun updateName(name: String): Result<AppUser> {
        val u = user.value?.copy(name = name) ?: return Result.failure(AuthException(AuthError.Unknown))
        save(u)
        u.phone?.removePrefix("+91")?.let { prefs.edit { putString("name_$it", name) } }
        return Result.success(u)
    }

    override suspend fun signOut() {
        val keepNames = prefs.all.filterKeys { it.startsWith("name_") }
        save(null)
        prefs.edit { keepNames.forEach { (k, v) -> putString(k, v as String) } }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        prefs.edit { clear() }
        user.value = null
        return Result.success(Unit)
    }

    companion object { const val TEST_OTP = "123456" }
}
