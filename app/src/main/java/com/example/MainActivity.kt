package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.session.Session
import com.example.ui.screens.FixhoraApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val application = applicationContext as FixhoraApplication
      // Starts from the default rather than blocking on disk, so the first frame is never delayed;
      // the stored preference arrives a frame later and re-themes.
      val session by application.sessionManager.session.collectAsState(initial = Session())

      MyApplicationTheme(themePreference = session.theme) { FixhoraApp() }
    }
  }
}
