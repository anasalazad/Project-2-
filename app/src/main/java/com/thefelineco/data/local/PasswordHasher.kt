package com.thefelineco.data.local

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Salted SHA-256 password hashing. Enough for a local, offline demo app. Passwords are never
 * stored in plain text.
 */
object PasswordHasher {
    private val random = SecureRandom()

    fun newSalt(): String = ByteArray(16).also(random::nextBytes).toHex()

    fun hash(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray(Charsets.UTF_8))
            .toHex()

    fun matches(password: String, salt: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(hash(password, salt).toByteArray(), expectedHash.toByteArray())

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
