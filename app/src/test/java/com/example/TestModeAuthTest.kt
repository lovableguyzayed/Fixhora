package com.example

import android.app.Activity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.auth.AuthError
import com.example.data.auth.AuthException
import com.example.data.auth.OtpResult
import com.example.data.auth.TestModeAuthRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowLooper

@RunWith(AndroidJUnit4::class)
class TestModeAuthTest {
    @Test fun otpLoginNameAndLogout() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val auth = TestModeAuthRepository(ctx)
        auth.deleteAccount()
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        var result: OtpResult? = null
        auth.sendOtp(activity, "9876543210", resend = false) { result = it }
        ShadowLooper.idleMainLooper(1, java.util.concurrent.TimeUnit.SECONDS)
        val id = (result as OtpResult.CodeSent).verificationId

        val wrong = auth.verifyOtp(id, "000000")
        assertEquals(AuthError.WrongCode, (wrong.exceptionOrNull() as AuthException).error)
        assertNull(auth.currentUser.value)

        val user = auth.verifyOtp(id, TestModeAuthRepository.TEST_OTP).getOrThrow()
        assertEquals("+919876543210", user.phone)
        assertNull("new user has no name yet", user.name)
        auth.updateName("Ramesh")
        assertEquals("Ramesh", auth.currentUser.value?.name)

        // Session survives an app restart.
        assertEquals("Ramesh", TestModeAuthRepository(ctx).currentUser.value?.name)

        auth.signOut()
        assertNull(auth.currentUser.value)
        // Returning user keeps their name.
        assertEquals("Ramesh", auth.verifyOtp(id, TestModeAuthRepository.TEST_OTP).getOrThrow().name)
    }
}
