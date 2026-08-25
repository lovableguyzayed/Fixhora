package com.example.ui.i18n

import android.content.Context
import android.content.res.Configuration
import android.view.ContextThemeWrapper
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
 * - `AppCompatDelegate.setApplicationLocales` needs AppCompat as the activity base class, which
 *   this app does not use; it is pure Compose on `ComponentActivity`.
 * - The platform per-app language API arrived in Android 13, and `minSdk` here is 24.
 *
 * This works on every supported API level.
 */
@Composable
fun LocalizedContent(language: AppLanguage, content: @Composable () -> Unit) {
  val context = LocalContext.current

  val localizedContext = remember(language, context) { context.withLocale(language) }

  // Both are needed: `stringResource` reads `LocalConfiguration` to know when to recompose and
  // `LocalContext.resources` to do the lookup. Providing only one leaves the app either showing
  // stale text or never updating at all.
  CompositionLocalProvider(
    LocalContext provides localizedContext,
    LocalConfiguration provides localizedContext.resources.configuration,
    content = content,
  )
}

/**
 * A view of this context whose resources are in [language], **still wrapping the original**.
 *
 * The wrapping is the whole point, and getting it wrong crashed the app on launch. The obvious
 * implementation is `context.createConfigurationContext(config)`, which returns a bare `ContextImpl`
 * — a context with no link back to the Activity it came from. Several androidx APIs find the
 * Activity by walking `ContextWrapper.getBaseContext()` up from `LocalContext.current`:
 * `rememberLauncherForActivityResult` resolves its `ActivityResultRegistryOwner` that way and
 * throws "No ActivityResultRegistryOwner was provided" when the walk comes up empty. `UpdateDialog`
 * calls it unconditionally at the root of the app, so with a bare `ContextImpl` in `LocalContext`
 * the very first composition threw and the process died before drawing a frame.
 *
 * `ContextThemeWrapper.applyOverrideConfiguration` is the long-standing way to do this — it is what
 * AppCompat itself uses for per-app locales below Android 13. The result is a real `ContextWrapper`
 * around this context, so the base-context walk still reaches the Activity, while `getResources()`
 * returns resources configured for the requested locale.
 *
 * Only the locale is overridden. A fresh [Configuration] leaves every other field unset, and
 * `applyOverrideConfiguration` merges just the fields that are set, so density, orientation, screen
 * size and night mode all keep coming from the real device configuration.
 */
private fun Context.withLocale(language: AppLanguage): Context {
  val locale = Locale.forLanguageTag(language.storageValue)
  val override = Configuration().apply { setLocale(locale) }
  // themeResId 0 means "keep the base context's theme", so the app's Material theme is unaffected.
  return ContextThemeWrapper(this, 0).apply { applyOverrideConfiguration(override) }
}
