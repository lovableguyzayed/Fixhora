package com.example.ui.i18n

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.data.session.AppLanguage
import java.util.Locale

/**
 * Makes the chosen language the one `stringResource` resolves against.
 *
 * Until now `AppLanguage` was stored in the session and read into `Session` — and then **nobody
 * used it**. The language pill on the role screen was worse still: it wrote to local `remember`
 * state, translated thirteen strings on that one screen with inline `if (isHindi)` ternaries, and
 * forgot the choice the moment you navigated away.
 *
 * This overrides the context and configuration that Compose resolves resources through, so a
 * single stored preference drives every `stringResource` in the app. That is the same shape the
 * theme already uses — one value in the session, applied once at the root.
 *
 * Chosen over the alternatives deliberately:
 * - `AppCompatDelegate.setApplicationLocales` needs AppCompat, which this app does not use; it is
 *   pure Compose on `ComponentActivity`, and pulling in AppCompat for one call is a poor trade.
 * - The platform per-app language API arrived in Android 13, and `minSdk` here is 24.
 *
 * This works on every supported API level and adds no dependency.
 */
@Composable
fun LocalizedContent(language: AppLanguage, content: @Composable () -> Unit) {
  val context = LocalContext.current

  val localizedContext =
    remember(language, context) {
      val configuration = Configuration(context.resources.configuration)
      configuration.setLocale(Locale.forLanguageTag(language.storageValue))
      context.createConfigurationContext(configuration)
    }

  // Both are needed: `stringResource` reads `LocalConfiguration` to know when to recompose and
  // `LocalContext.resources` to do the lookup. Providing only one leaves the app either showing
  // stale text or never updating at all.
  CompositionLocalProvider(
    LocalContext provides localizedContext,
    LocalConfiguration provides localizedContext.resources.configuration,
    content = content,
  )
}
