package com.example.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.data.room.UserDao
import com.example.data.room.UserEntity
import com.example.data.security.PasswordHasher
import com.example.util.normalizeMobile
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Outcome of a registration or sign-in attempt. */
sealed interface AuthResult {
  data class Success(val user: UserEntity) : AuthResult

  /** Registration: that mobile number already has an account. */
  data object MobileAlreadyRegistered : AuthResult

  /** Sign-in: no such account, or the password did not match. Deliberately not distinguished. */
  data object InvalidCredentials : AuthResult

  data class Failure(val cause: Throwable) : AuthResult
}

class UserRepository(private val userDao: UserDao) {

  fun observeUser(id: String): Flow<UserEntity?> = userDao.observeById(id)

  suspend fun findById(id: String): UserEntity? = userDao.findById(id)

  /**
   * Creates an account. Password derivation runs on [Dispatchers.Default] because PBKDF2 is
   * intentionally expensive and would otherwise stall the caller's thread.
   */
  suspend fun register(
    fullName: String,
    mobile: String,
    email: String?,
    password: String,
  ): AuthResult =
    withContext(Dispatchers.Default) {
      val normalizedMobile = normalizeMobile(mobile)
      try {
        if (userDao.findByMobile(normalizedMobile) != null) {
          return@withContext AuthResult.MobileAlreadyRegistered
        }
        val salt = PasswordHasher.newSalt()
        val user =
          UserEntity(
            id = UUID.randomUUID().toString(),
            fullName = fullName.trim(),
            mobile = normalizedMobile,
            email = email?.trim()?.takeIf { it.isNotEmpty() },
            passwordHash = PasswordHasher.hash(password, salt),
            passwordSalt = salt,
            gender = null,
            dateOfBirth = null,
            city = "",
            state = "",
            pinCode = "",
            preferredLanguage = "en",
            createdAt = System.currentTimeMillis(),
          )
        userDao.insert(user)
        AuthResult.Success(user)
      } catch (e: SQLiteConstraintException) {
        // The unique index on `mobile` is the real guard: two concurrent registrations with the
        // same number both pass the check above, and exactly one reaches this branch.
        AuthResult.MobileAlreadyRegistered
      } catch (e: Exception) {
        AuthResult.Failure(e)
      }
    }

  /**
   * Verifies credentials.
   *
   * An unknown mobile number and a wrong password both return [AuthResult.InvalidCredentials], so
   * the response cannot be used to discover which numbers have accounts.
   */
  suspend fun login(mobile: String, password: String): AuthResult =
    withContext(Dispatchers.Default) {
      try {
        val user =
          userDao.findByMobile(normalizeMobile(mobile))
            ?: return@withContext AuthResult.InvalidCredentials
        if (PasswordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
          AuthResult.Success(user)
        } else {
          AuthResult.InvalidCredentials
        }
      } catch (e: Exception) {
        AuthResult.Failure(e)
      }
    }

  /** Saves the profile fields collected after sign-up. Returns false if the account is gone. */
  suspend fun updateProfile(
    userId: String,
    fullName: String,
    gender: String?,
    dateOfBirth: String?,
    city: String,
    state: String,
    pinCode: String,
    preferredLanguage: String,
  ): Boolean {
    val existing = userDao.findById(userId) ?: return false
    userDao.update(
      existing.copy(
        fullName = fullName.trim().ifEmpty { existing.fullName },
        gender = gender?.trim()?.takeIf { it.isNotEmpty() },
        dateOfBirth = dateOfBirth?.trim()?.takeIf { it.isNotEmpty() },
        city = city.trim(),
        state = state.trim(),
        pinCode = pinCode.trim(),
        preferredLanguage = preferredLanguage,
      )
    )
    return true
  }
}
