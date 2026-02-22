package com.verobee.vergate.adapters.out.cache

import com.verobee.vergate.ports.out.AppRepositoryPort
import com.verobee.vergate.ports.out.GatewayCachePort
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class ValkeyCacheAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val appRepositoryPort: AppRepositoryPort,
) : GatewayCachePort {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val INIT_PREFIX = "vergate:init:"
        private const val APP_ID_INDEX_PREFIX = "vergate:app-keys:"
    }

    override fun getInitResponse(cacheKey: String): String? =
        try {
            redisTemplate.opsForValue().get("$INIT_PREFIX$cacheKey")
        } catch (e: Exception) {
            log.warn("Valkey GET failed for key={}, falling through to DB", cacheKey, e)
            null
        }

    override fun setInitResponse(cacheKey: String, json: String, ttlSeconds: Long) {
        try {
            redisTemplate.opsForValue().set(
                "$INIT_PREFIX$cacheKey",
                json,
                Duration.ofSeconds(ttlSeconds),
            )
        } catch (e: Exception) {
            log.warn("Valkey SET failed for key={}", cacheKey, e)
        }
    }

    override fun evictByAppKey(appKey: String) {
        try {
            val pattern = "$INIT_PREFIX$appKey:*"
            val keys = redisTemplate.keys(pattern)
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys)
                log.debug("Evicted {} cache entries for appKey={}", keys.size, appKey)
            }
        } catch (e: Exception) {
            log.warn("Valkey evict failed for appKey={}", appKey, e)
        }
    }

    override fun evictByAppId(appId: String) {
        val app = appRepositoryPort.findById(appId) ?: return
        evictByAppKey(app.appKey)
    }
}
