package com.example.util

import android.content.Context
import androidx.core.content.edit

/** Small settings kept on the phone: which side of the app was used last, helper online switch. */
class AppPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    /** "customer", "helper" or null when the person has not chosen yet. */
    var lastRole: String?
        get() = prefs.getString("last_role", null)
        set(value) = prefs.edit { putString("last_role", value) }

    var helperOnline: Boolean
        get() = prefs.getBoolean("helper_online", true)
        set(value) = prefs.edit { putBoolean("helper_online", value) }

    fun clear() = prefs.edit { clear() }
}
