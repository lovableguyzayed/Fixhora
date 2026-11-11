package com.example

import androidx.compose.runtime.Composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.TaskFlowContainer
import com.example.ui.screens.auth.MobileLoginScreen
import com.example.ui.screens.auth.OtpVerificationScreen
import com.example.ui.screens.auth.ProfileSetupScreen
import com.example.ui.screens.auth.SignInScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders key screens to PNG (no phone needed) for the audit's before/after previews.
 * Run: ./gradlew testDebugUnitTest --tests com.example.ScreenshotTest
 * Output: app/build/screenshots/<size>/<screen>.png
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTest {

    private val screens: Map<String, @Composable () -> Unit> = mapOf(
        "welcome" to { WelcomeScreen({}, {}, {}) },
        "sign_in" to { SignInScreen({}, {}, {}, {}) },
        "mobile_login" to { MobileLoginScreen({}, {}) },
        "otp" to { OtpVerificationScreen({}, {}, mobile = "9876543210") },
        "profile_setup" to { ProfileSetupScreen({}) },
        "role_selection" to { RoleSelectionScreen({}) },
        "task_category" to { TaskFlowContainer({}) },
    )

    private fun shoot(folder: String) {
        screens.forEach { (name, content) ->
            captureRoboImage("build/screenshots/$folder/$name.png") {
                MyApplicationTheme { content() }
            }
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xhdpi")
    fun phone360x800() = shoot("360x800")

    @Test
    @Config(qualifiers = "w320dp-h568dp-xhdpi")
    fun smallPhone320x568() = shoot("320x568")

    @Test
    @Config(qualifiers = "w360dp-h800dp-xhdpi")
    fun phoneMaxFont() {
        RuntimeEnvironment.setFontScale(2.0f)
        shoot("360x800_font200")
    }
}
