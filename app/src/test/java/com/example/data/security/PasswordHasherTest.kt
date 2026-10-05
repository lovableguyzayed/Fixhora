package com.example.data.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

  private val password = "correct horse battery staple"

  @Test
  fun `salts are random and hex encoded`() {
    val a = PasswordHasher.newSalt()
    val b = PasswordHasher.newSalt()

    assertEquals(32, a.length)
    assertTrue(a.all { it in "0123456789abcdef" })
    assertNotEquals(a, b)
  }

  @Test
  fun `hash is not the plaintext`() {
    val salt = PasswordHasher.newSalt()

    val hash = PasswordHasher.hash(password, salt)

    assertEquals(64, hash.length)
    assertNotEquals(password, hash)
    assertFalse(hash.contains("horse"))
  }

  @Test
  fun `same password and salt always derive the same hash`() {
    val salt = PasswordHasher.newSalt()

    assertEquals(PasswordHasher.hash(password, salt), PasswordHasher.hash(password, salt))
  }

  @Test
  fun `salting means identical passwords do not share a hash`() {
    val saltA = PasswordHasher.newSalt()
    val saltB = PasswordHasher.newSalt()

    assertNotEquals(PasswordHasher.hash(password, saltA), PasswordHasher.hash(password, saltB))
  }

  @Test
  fun `correct password verifies`() {
    val salt = PasswordHasher.newSalt()
    val hash = PasswordHasher.hash(password, salt)

    assertTrue(PasswordHasher.verify(password, salt, hash))
  }

  @Test
  fun `wrong password is rejected`() {
    val salt = PasswordHasher.newSalt()
    val hash = PasswordHasher.hash(password, salt)

    assertFalse(PasswordHasher.verify("Correct horse battery staple", salt, hash))
    assertFalse(PasswordHasher.verify("", salt, hash))
    assertFalse(PasswordHasher.verify("$password ", salt, hash))
  }

  @Test
  fun `right password against the wrong salt is rejected`() {
    val salt = PasswordHasher.newSalt()
    val hash = PasswordHasher.hash(password, salt)

    assertFalse(PasswordHasher.verify(password, PasswordHasher.newSalt(), hash))
  }

  @Test
  fun `corrupt stored values fail the login instead of throwing`() {
    val salt = PasswordHasher.newSalt()
    val hash = PasswordHasher.hash(password, salt)

    assertFalse(PasswordHasher.verify(password, "", hash))
    assertFalse(PasswordHasher.verify(password, salt, ""))
    assertFalse(PasswordHasher.verify(password, "abc", hash))
    assertFalse(PasswordHasher.verify(password, "zzzz", hash))
  }

  @Test
  fun `unicode and very long passwords round-trip`() {
    val salt = PasswordHasher.newSalt()
    val unicode = "पासवर्ड-🔐-ਪਾਸ"
    val long = "x".repeat(4096)

    assertTrue(PasswordHasher.verify(unicode, salt, PasswordHasher.hash(unicode, salt)))
    assertTrue(PasswordHasher.verify(long, salt, PasswordHasher.hash(long, salt)))
  }

  /**
   * Guards the KDF itself. Every other test here would still pass on a subtly wrong PBKDF2, so
   * these published PBKDF2-HMAC-SHA256 vectors (P = "password", S = "salt", dkLen = 32) are what
   * actually prove the implementation is correct.
   */
  @Test
  fun `matches published PBKDF2-HMAC-SHA256 vectors`() {
    val saltHex = "73616c74" // "salt"

    assertEquals(
      "120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b",
      PasswordHasher.hash("password", saltHex, iterations = 1),
    )
    assertEquals(
      "ae4d0c95af6b46d32d0adff928f06dd02a303f8ef3c251dfd6e2d85a95474c43",
      PasswordHasher.hash("password", saltHex, iterations = 2),
    )
    assertEquals(
      "c5e478d59288c841aa530db6845c4c8d962893a001ce4e11a4963873aa98134a",
      PasswordHasher.hash("password", saltHex, iterations = 4096),
    )
  }
}
