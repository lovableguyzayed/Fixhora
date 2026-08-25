package com.example.ui.i18n

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.R
import com.example.data.session.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Covers the two things [LocalizedContent] has to do at once, which pull in opposite directions:
 * swap the resources Compose resolves against, and leave the Activity reachable from `LocalContext`.
 *
 * The first version satisfied only the first. It provided the result of `createConfigurationContext`
 * — a bare `ContextImpl` with no link back to the Activity — and the app died on launch before
 * drawing a frame, because `UpdateDialog` sits at the root and calls
 * `rememberLauncherForActivityResult`, which finds its `ActivityResultRegistryOwner` by walking
 * `ContextWrapper.getBaseContext()` up from `LocalContext.current`.
 *
 * Nothing caught it: the unit tests never composed anything, and CI builds the APK but never
 * launches it. So the test for it is a test that actually composes.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class LocalizedContentTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  /**
   * The regression test for the launch crash.
   *
   * `rememberLauncherForActivityResult` throws `IllegalStateException: No
   * ActivityResultRegistryOwner was provided via LocalActivityResultRegistryOwner` when the
   * Activity cannot be found from the ambient context. Registering one inside [LocalizedContent] is
   * exactly what the real app does on its first frame, so composing it here either works or fails
   * the way the app failed.
   */
  @Test
  fun `an activity result launcher can still be registered inside LocalizedContent`() {
    compose.setContent {
      LocalizedContent(AppLanguage.HINDI) {
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
      }
    }
    compose.waitForIdle()
  }

  /** The same, for the language the app starts in. */
  @Test
  fun `an activity result launcher can still be registered in English`() {
    compose.setContent {
      LocalizedContent(AppLanguage.ENGLISH) {
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
      }
    }
    compose.waitForIdle()
  }

  /**
   * The feature itself, so a fix for the crash cannot quietly cost the translation. `nav_home` is
   * used rather than a brand word precisely because it is one of the strings that must differ.
   */
  @Test
  fun `strings resolve in the chosen language`() {
    var hindi = ""
    var english = ""
    compose.setContent {
      LocalizedContent(AppLanguage.HINDI) { hindi = stringResource(R.string.nav_home) }
      LocalizedContent(AppLanguage.ENGLISH) { english = stringResource(R.string.nav_home) }
    }
    compose.waitForIdle()

    assertEquals("होम", hindi)
    assertEquals("Home", english)
  }

  /**
   * Only the locale may be overridden. A `Configuration` copied wholesale, or an override applied
   * to the wrong base, would take the density with it and every `dp` in the app would resolve
   * against a stale screen — a far quieter bug than a crash.
   */
  @Test
  fun `overriding the language leaves the rest of the configuration alone`() {
    var localizedDensity = 0
    compose.setContent {
      LocalizedContent(AppLanguage.HINDI) {
        localizedDensity = LocalConfiguration.current.densityDpi
      }
    }
    compose.waitForIdle()
    val deviceDensity = compose.activity.resources.configuration.densityDpi

    assertEquals(deviceDensity, localizedDensity)
  }
}
