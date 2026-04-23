package ru.hse.fandomatch.data

import java.security.MessageDigest

object PasswordHasher {
    private const val TEMP_PEPPER = "FandoMatch_temp_client_hash_v1"

    fun sha256(rawPassword: String): String {
        val input = "$TEMP_PEPPER:$rawPassword"
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
