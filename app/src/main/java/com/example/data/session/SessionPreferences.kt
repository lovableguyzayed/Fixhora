package com.example.data.session

/**
 * Which side of the marketplace the user is currently acting as. A single account can be both, so
 * this is a mode, not a permission.
 */
enum class UserRole(val storageValue: String) {
  CUSTOMER("user"),
  HELPER("helper");

  companion object {
    private val BY_STORAGE_VALUE = entries.associateBy { it.storageValue }

    fun fromStorage(value: String?): UserRole? = BY_STORAGE_VALUE[value]
  }
}

enum class ThemePreference(val storageValue: String) {
  SYSTEM("system"),
  LIGHT("light"),
  DARK("dark");

  companion object {
    private val BY_STORAGE_VALUE = entries.associateBy { it.storageValue }

    fun fromStorage(value: String?): ThemePreference = BY_STORAGE_VALUE[value] ?: SYSTEM
  }
}

enum class AppLanguage(val storageValue: String) {
  ENGLISH("en"),
  HINDI("hi");

  companion object {
    private val BY_STORAGE_VALUE = entries.associateBy { it.storageValue }

    fun fromStorage(value: String?): AppLanguage = BY_STORAGE_VALUE[value] ?: ENGLISH
  }
}

/**
 * The persisted session. [userId] is null while browsing as a guest, which is a supported state:
 * the Welcome screen offers "Continue as Guest".
 */
data class Session(
  val userId: String? = null,
  val activeRole: UserRole? = null,
  val language: AppLanguage = AppLanguage.ENGLISH,
  val theme: ThemePreference = ThemePreference.SYSTEM,
) {
  val isSignedIn: Boolean
    get() = userId != null

  /** Owner id to stamp on tasks created in this session. */
  fun ownerId(guestId: String): String = userId ?: guestId
}
