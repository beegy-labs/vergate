package com.verobee.vergate.domain.service

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.common.exception.ErrorCode
import com.verobee.vergate.domain.model.RemoteConfig
import com.verobee.vergate.domain.model.ValueType
import com.verobee.vergate.domain.util.AppKeyGenerator
import com.verobee.vergate.ports.out.GatewayCachePort
import com.verobee.vergate.ports.out.RemoteConfigRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class RemoteConfigService(
    private val configRepository: RemoteConfigRepositoryPort,
    private val cache: GatewayCachePort,
) {

    fun findByAppId(appId: String): List<RemoteConfig> = configRepository.findByAppId(appId)

    fun findById(id: String): RemoteConfig =
        configRepository.findById(id) ?: throw ApiException(ErrorCode.CONFIG_NOT_FOUND)

    @Transactional
    fun create(appId: String, configKey: String, configValue: String, valueType: String, description: String?, abRatio: Int): RemoteConfig {
        if (configRepository.existsByAppIdAndConfigKey(appId, configKey)) {
            throw ApiException(ErrorCode.CONFIG_KEY_DUPLICATE)
        }
        val saved = configRepository.save(
            RemoteConfig(
                id = AppKeyGenerator.generateUuidV7().toString(),
                appId = appId, configKey = configKey, configValue = configValue,
                valueType = ValueType.valueOf(valueType.uppercase()), description = description, abRatio = abRatio,
            )
        )
        cache.evictByAppId(appId)
        return saved
    }

    @Transactional
    fun update(id: String, configValue: String, valueType: String, description: String?, abRatio: Int, isActive: Boolean): RemoteConfig {
        val existing = findById(id)
        val updated = existing.copy(
            configValue = configValue, valueType = ValueType.valueOf(valueType.uppercase()),
            description = description, abRatio = abRatio, isActive = isActive, updatedAt = OffsetDateTime.now(),
        )
        val saved = configRepository.save(updated)
        cache.evictByAppId(saved.appId)
        return saved
    }

    @Transactional
    fun delete(id: String) {
        val c = findById(id)
        cache.evictByAppId(c.appId)
        configRepository.deleteById(id)
    }
}
