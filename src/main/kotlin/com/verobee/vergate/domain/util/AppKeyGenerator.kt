package com.verobee.vergate.domain.util

import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.UUID

/**
 * Generates UUIDv7-based identifiers.
 *
 * - Internal ID  : UUIDv7 (time-ordered, globally unique)
 * - Public appKey: Base62 encoding of the UUIDv7 bytes → 22 URL-safe characters
 *
 * UUIDv7 layout (RFC 9562):
 *   [48-bit unix_ts_ms][4-bit version=7][12-bit rand_a][2-bit variant=10][62-bit rand_b]
 */
object AppKeyGenerator {

    private val random = SecureRandom()
    private const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private val BASE = BigInteger.valueOf(62)
    private const val KEY_LENGTH = 22

    /** Generate a new UUIDv7. */
    fun generateUuidV7(): UUID {
        val now = System.currentTimeMillis()
        val rand12 = random.nextLong() and 0xFFFL
        val msb = (now shl 16) or 0x7000L or rand12

        val lsb = (random.nextLong() and 0x3FFFFFFFFFFFFFFFL) or Long.MIN_VALUE
        return UUID(msb, lsb)
    }

    /**
     * Encode a UUID to a 22-character Base62 string.
     * The full 128 bits are preserved → no collision risk.
     */
    fun toAppKey(uuid: UUID): String {
        val bytes = ByteBuffer.allocate(16)
            .putLong(uuid.mostSignificantBits)
            .putLong(uuid.leastSignificantBits)
            .array()

        var n = BigInteger(1, bytes)
        val sb = StringBuilder()
        while (n.signum() > 0) {
            val (q, r) = n.divideAndRemainder(BASE)
            sb.append(ALPHABET[r.toInt()])
            n = q
        }
        while (sb.length < KEY_LENGTH) sb.append('0')
        return sb.reverse().toString()
    }

    /** Generate a UUIDv7 and its Base62 appKey in one call. */
    fun generate(): Pair<UUID, String> {
        val uuid = generateUuidV7()
        return uuid to toAppKey(uuid)
    }
}
