package com.bockerl.snailmember.security.config

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

fun main() {
    val keyGenerator = HMACKeyGenerator()
    keyGenerator.generateSecretKey()
}

class HMACKeyGenerator {
    private val logger = KotlinLogging.logger {}

    fun generateSecretKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance("HmacSHA512")
            keyGenerator.init(256)
            val secretKey: SecretKey = keyGenerator.generateKey()
            val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
            logger.info { "Secret key generated: $encodedKey" }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
