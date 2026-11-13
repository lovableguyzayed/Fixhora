package com.example.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Real login with Firebase Authentication: phone OTP (Firebase reads the SMS automatically on
 * Android when it can) and "Sign in with Google" through Android's Credential Manager.
 * The OTP is checked by Firebase's servers, which also apply rate limits per number/device.
 */
class FirebaseAuthRepository(private val context: Context) : AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    override val isTestMode = false

    private val user = MutableStateFlow(auth.currentUser?.toAppUser())
    override val currentUser: StateFlow<AppUser?> = user.asStateFlow()
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    init {
        auth.addAuthStateListener { user.value = it.currentUser?.toAppUser() }
    }

    override fun sendOtp(activity: Activity, phone10: String, resend: Boolean, onResult: (OtpResult) -> Unit) {
        val builder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phone10")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential).addOnCompleteListener { t ->
                        val u = t.result?.user?.toAppUser()
                        onResult(
                            if (t.isSuccessful && u != null) OtpResult.AutoVerified(u, credential.smsCode)
                            else OtpResult.Failed(mapError(t.exception))
                        )
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) = onResult(OtpResult.Failed(mapError(e)))

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    resendToken = token
                    onResult(OtpResult.CodeSent(id))
                }
            })
        if (resend) resendToken?.let { builder.setForceResendingToken(it) }
        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    override suspend fun verifyOtp(verificationId: String, code: String): Result<AppUser> = runAuth {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential).await().user!!.toAppUser()
    }

    // The web client id is generated from google-services.json only when Firebase is set up,
    // so it cannot be referenced as R.string at compile time.
    @android.annotation.SuppressLint("DiscouragedApi")
    override suspend fun signInWithGoogle(activity: Activity): Result<AppUser> = runAuth {
        // Created by the google-services plugin from google-services.json.
        val webClientIdRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (webClientIdRes == 0) throw AuthException(AuthError.NotConfigured)
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(webClientIdRes))
            .setFilterByAuthorizedAccounts(false)
            .build()
        val response = CredentialManager.create(activity)
            .getCredential(activity, GetCredentialRequest(listOf(option)))
        val credential = response.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) throw AuthException(AuthError.Unknown)
        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user!!.toAppUser()
    }

    override suspend fun updateName(name: String): Result<AppUser> = runAuth {
        val u = auth.currentUser ?: throw AuthException(AuthError.Unknown)
        u.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
        u.reload().await()
        val updated = auth.currentUser!!.toAppUser()
        user.value = updated // a name change does not trigger the auth-state listener
        updated
    }

    override suspend fun signOut() {
        auth.signOut()
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }

    override suspend fun deleteAccount(): Result<Unit> = runAuth {
        auth.currentUser?.delete()?.await()
        Unit
    }

    private suspend fun <T> runAuth(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: AuthException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AuthException(mapError(e), e))
    }

    private fun mapError(e: Throwable?): AuthError = when (e) {
        is AuthException -> e.error
        is FirebaseAuthRecentLoginRequiredException -> AuthError.RecentLoginRequired
        is FirebaseAuthInvalidCredentialsException -> when (e.errorCode) {
            "ERROR_INVALID_VERIFICATION_CODE" -> AuthError.WrongCode
            "ERROR_SESSION_EXPIRED" -> AuthError.CodeExpired
            "ERROR_INVALID_PHONE_NUMBER" -> AuthError.InvalidNumber
            else -> AuthError.WrongCode
        }
        is FirebaseTooManyRequestsException -> AuthError.TooManyRequests
        is FirebaseNetworkException -> AuthError.Network
        is GetCredentialCancellationException -> AuthError.Cancelled
        is NoCredentialException -> AuthError.NoGoogleAccount
        else -> AuthError.Unknown
    }

    private fun FirebaseUser.toAppUser() = AppUser(
        uid = uid,
        phone = phoneNumber?.takeIf { it.isNotBlank() },
        email = email?.takeIf { it.isNotBlank() },
        name = displayName?.takeIf { it.isNotBlank() },
    )
}

/** Small bridge from Google Play "Task" callbacks to Kotlin coroutines. */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { t ->
        if (t.isSuccessful) cont.resume(t.result)
        else cont.resumeWithException(t.exception ?: IllegalStateException("Task failed"))
    }
}
