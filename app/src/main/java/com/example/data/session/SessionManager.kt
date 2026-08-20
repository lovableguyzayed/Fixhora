package com.example.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "fixhora_session")

/**
 * Persists who is signed in and their app-level preferences, so a restart does not silently drop
 * the user back to the sign-in screen.
 *
 * Reads fall back to defaults on [IOException] rather than propagating: a corrupt or unreadable
 * preferences file should sign the user out, not crash the app on launch.
 */
class SessionManager(context: Context) {

  private val dataStore = context.applicationContext.sessionDataStore

  private object Keys {
    val USER_ID = stringPreferencesKey("logged_in_user_id")
    val ACTIVE_ROLE = stringPreferencesKey("active_role")
    val LANGUAGE = stringPreferencesKey("language")
    val THEME = stringPreferencesKey("theme")
  }

  val session: Flow<Session> =
    dataStore.data
      .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
      .map { prefs ->
        Session(
          userId = prefs[Keys.USER_ID],
          activeRole = UserRole.fromStorage(prefs[Keys.ACTIVE_ROLE]),
          language = AppLanguage.fromStorage(prefs[Keys.LANGUAGE]),
          theme = ThemePreference.fromStorage(prefs[Keys.THEME]),
        )
      }

  suspend fun current(): Session = session.first()

  suspend fun signIn(userId: String) {
    dataStore.edit { it[Keys.USER_ID] = userId }
  }

  /** Clears identity and the chosen role, but keeps language and theme: those are device choices. */
  suspend fun signOut() {
    dataStore.edit {
      it.remove(Keys.USER_ID)
      it.remove(Keys.ACTIVE_ROLE)
    }
  }

  suspend fun setActiveRole(role: UserRole) {
    dataStore.edit { it[Keys.ACTIVE_ROLE] = role.storageValue }
  }

  suspend fun setLanguage(language: AppLanguage) {
    dataStore.edit { it[Keys.LANGUAGE] = language.storageValue }
  }

  suspend fun setTheme(theme: ThemePreference) {
    dataStore.edit { it[Keys.THEME] = theme.storageValue }
  }
}
