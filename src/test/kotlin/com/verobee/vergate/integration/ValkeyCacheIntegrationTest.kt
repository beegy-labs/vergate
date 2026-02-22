package com.verobee.vergate.integration

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

@SpringBootTest
class ValkeyCacheIntegrationTest : TestcontainersConfig() {

    @Autowired
    private lateinit var redisTemplate: StringRedisTemplate

    @Test
    fun `set with TTL expires after duration`() {
        val key = "vergate:init:test-ttl:ANDROID:1.0.0"
        redisTemplate.opsForValue().set(key, """{"test":true}""", Duration.ofSeconds(2))

        assertNotNull(redisTemplate.opsForValue().get(key))

        Thread.sleep(2500)

        assertNull(redisTemplate.opsForValue().get(key))
    }

    @Test
    fun `pattern-based eviction deletes matching keys`() {
        val prefix = "vergate:init:evict-test"
        redisTemplate.opsForValue().set("$prefix:ANDROID:1.0.0", "a", Duration.ofMinutes(1))
        redisTemplate.opsForValue().set("$prefix:ANDROID:2.0.0", "b", Duration.ofMinutes(1))
        redisTemplate.opsForValue().set("$prefix:IOS:1.0.0", "c", Duration.ofMinutes(1))
        redisTemplate.opsForValue().set("vergate:init:other-app:ANDROID:1.0.0", "d", Duration.ofMinutes(1))

        val keys = redisTemplate.keys("$prefix:*")
        assertEquals(3, keys.size)

        redisTemplate.delete(keys)

        assertTrue(redisTemplate.keys("$prefix:*").isEmpty())
        assertNotNull(redisTemplate.opsForValue().get("vergate:init:other-app:ANDROID:1.0.0"))

        // cleanup
        redisTemplate.delete("vergate:init:other-app:ANDROID:1.0.0")
    }
}
