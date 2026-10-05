package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.session.Session
import com.example.ui.i18n.LocalizedContent
import com.example.ui.screens.FixhoraApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.isDarkTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val application = applicationContext as FixhoraApplication
      // Starts from the default rather than blocking on disk, so the first frame is never delayed;
      // the stored preference arrives a frame later and re-themes.
      val session by application.sessionManager.session.collectAsState(initial = Session())

      // The in-app theme can differ from the system one (Settings -> Dark while the phone is
      // light, or the reverse). Plain enableEdgeToEdge() follows the system, which then draws
      // light status-bar icons on a light screen. Re-applying it per theme keeps them readable.
      val darkTheme = session.theme.isDarkTheme()
      DisposableEffect(darkTheme) {
        enableEdgeToEdge(
          statusBarStyle =
            SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
          navigationBarStyle = SystemBarStyle.auto(LightNavScrim, DarkNavScrim) { darkTheme },
        )
        onDispose {}
      }

      // Language wraps the theme, not the other way round: the theme reads no strings, but every
      // screen inside it does.
      LocalizedContent(language = session.language) {
        MyApplicationTheme(themePreference = session.theme) { FixhoraApp() }
      }
    }
  }
}

/** The scrims AndroidX applies behind three-button navigation by default. */
private val LightNavScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DarkNavScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
