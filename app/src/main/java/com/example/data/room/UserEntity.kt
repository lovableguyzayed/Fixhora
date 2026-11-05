package com.example.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A registered account.
 *
 * The password is never stored; only [passwordSalt] and the derived [passwordHash] are, so the
 * database cannot give up a usable password even if the device or a backup is compromised. See
 * `com.example.data.security.PasswordHasher`.
 *
 * [mobile] is uniquely indexed because it is the login identifier.
 */
@Entity(tableName = "users", indices = [Index(value = ["mobile"], unique = true)])
data class UserEntity(
  @PrimaryKey val id: String,
  val fullName: String,
  val mobile: String,
  val email: String?,
  val passwordHash: String,
  val passwordSalt: String,
  val gender: String?,
  val dateOfBirth: String?,
  val city: String,
  val state: String,
  val pinCode: String,
  val preferredLanguage: String,
  val createdAt: Long,
  /** Category ids a helper offers, joined by a newline; empty for someone who only posts. */
  val skills: String? = null,
  /** A line or two a helper writes about themselves. */
  val about: String? = null,
) {
  companion object {
    const val SKILL_SEPARATOR = "\n"
  }
}

/** The category ids in [UserEntity.skills]. */
fun UserEntity.skillIds(): List<String> =
  skills.orEmpty().split(UserEntity.SKILL_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }

/** Joins category ids for storage in [UserEntity.skills]; null when there are none. */
fun List<String>.joinSkillIds(): String? =
  filter { it.isNotBlank() }.distinct().joinToString(UserEntity.SKILL_SEPARATOR).ifEmpty { null }
