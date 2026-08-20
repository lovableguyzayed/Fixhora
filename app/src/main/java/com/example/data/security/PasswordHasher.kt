package com.example.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Salted PBKDF2-HMAC-SHA256 password hashing.
 *
 * The app stores accounts locally, which is exactly why passwords must not be stored in plain
 * text: anything readable in the app's database is readable by anyone who gets the device or a
 * backup of it, and people reuse passwords across services.
 *
 * PBKDF2 is implemented here over [Mac] rather than through `SecretKeyFactory`, because the
 * `PBKDF2WithHmacSHA256` factory only exists from API 26 while this app supports API 24. Going
 * through `Mac` gives one code path on every supported API level, and keeps this file free of
 * Android imports so its behaviour can be tested on a plain JVM.
 */
object PasswordHasher {

  /**
   * Deliberately expensive so that guessing a stolen hash is slow. Measured against the cost of
   * a single sign-in on a low-end device, where this runs off the main thread.
   */
  const val ITERATIONS: Int = 100_000

  private const val SALT_BYTES = 16
  private const val KEY_BYTES = 32
  private const val HMAC_ALGORITHM = "HmacSHA256"

  private val secureRandom = SecureRandom()
  private val HEX_DIGITS = "0123456789abcdef".toCharArray()

  /** A fresh random salt, hex-encoded, to be stored alongside the hash. */
  fun newSalt(): String {
    val salt = ByteArray(SALT_BYTES)
    secureRandom.nextBytes(salt)
    return salt.toHex()
  }

  /**
   * Derives the hex-encoded hash of [password] under [saltHex].
   *
   * The same password and salt always produce the same result, which is what makes [verify]
   * possible without ever storing the password.
   *
   * [iterations] is overridable only so that tests can check this implementation against the
   * published PBKDF2-HMAC-SHA256 vectors, which use small iteration counts. Production callers
   * must use the default: a stored hash is only reproducible at the count it was created with.
   */
  fun hash(password: String, saltHex: String, iterations: Int = ITERATIONS): String =
    pbkdf2(password.toCharArray(), saltHex.hexToBytes(), iterations, KEY_BYTES).toHex()

  /**
   * True when [password] matches [expectedHashHex] under [saltHex].
   *
   * Comparison is constant-time so that a caller cannot learn how much of a guess was correct by
   * timing the response. Malformed stored values return false instead of throwing, so a corrupt
   * row fails the login rather than crashing it.
   */
  fun verify(password: String, saltHex: String, expectedHashHex: String): Boolean {
    if (saltHex.isEmpty() || expectedHashHex.isEmpty()) return false
    val actual =
      try {
        hash(password, saltHex)
      } catch (e: IllegalArgumentException) {
        return false
      }
    return MessageDigest.isEqual(
      actual.toByteArray(Charsets.US_ASCII),
      expectedHashHex.lowercase().toByteArray(Charsets.US_ASCII),
    )
  }

  /** PBKDF2 as defined in RFC 8018, section 5.2. */
  private fun pbkdf2(
    password: CharArray,
    salt: ByteArray,
    iterations: Int,
    keyLength: Int,
  ): ByteArray {
    val mac = Mac.getInstance(HMAC_ALGORITHM)
    mac.init(SecretKeySpec(password.toByteArrayUtf8(), HMAC_ALGORITHM))

    val hashLength = mac.macLength
    val blockCount = (keyLength + hashLength - 1) / hashLength
    val output = ByteArray(blockCount * hashLength)

    for (block in 1..blockCount) {
      // U1 = PRF(password, salt || INT_32_BE(blockIndex))
      mac.update(salt)
      mac.update((block ushr 24).toByte())
      mac.update((block ushr 16).toByte())
      mac.update((block ushr 8).toByte())
      mac.update(block.toByte())
      var u = mac.doFinal()
      val accumulated = u.copyOf()

      // Ui = PRF(password, Ui-1), accumulated by XOR.
      for (round in 2..iterations) {
        u = mac.doFinal(u)
        for (i in accumulated.indices) {
          accumulated[i] = (accumulated[i].toInt() xor u[i].toInt()).toByte()
        }
      }
      accumulated.copyInto(output, (block - 1) * hashLength)
    }
    return output.copyOf(keyLength)
  }

  private fun CharArray.toByteArrayUtf8(): ByteArray = String(this).toByteArray(Charsets.UTF_8)

  private fun ByteArray.toHex(): String {
    val chars = CharArray(size * 2)
    for (i in indices) {
      val v = this[i].toInt() and 0xFF
      chars[i * 2] = HEX_DIGITS[v ushr 4]
      chars[i * 2 + 1] = HEX_DIGITS[v and 0x0F]
    }
    return String(chars)
  }

  private fun String.hexToBytes(): ByteArray {
    require(length % 2 == 0) { "Hex string must have an even length" }
    val out = ByteArray(length / 2)
    for (i in out.indices) {
      val hi = Character.digit(this[i * 2], 16)
      val lo = Character.digit(this[i * 2 + 1], 16)
      require(hi >= 0 && lo >= 0) { "Hex string contains a non-hex character" }
      out[i] = ((hi shl 4) or lo).toByte()
    }
    return out
  }
}
