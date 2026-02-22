package com.verobee.vergate.adapters.out.persistence.adapter

import com.verobee.vergate.adapters.out.persistence.mapper.RemoteConfigMapper
import com.verobee.vergate.adapters.out.persistence.repository.RemoteConfigJpaRepository
import com.verobee.vergate.domain.model.RemoteConfig
import com.verobee.vergate.ports.out.RemoteConfigRepositoryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class RemoteConfigJpaAdapter(
    private val jpaRepository: RemoteConfigJpaRepository,
    private val mapper: RemoteConfigMapper,
) : RemoteConfigRepositoryPort {

    override fun findById(id: String): RemoteConfig? =
        jpaRepository.findByIdOrNull(UUID.fromString(id))?.let(mapper::toDomain)

    override fun findByAppId(appId: String): List<RemoteConfig> =
        jpaRepository.findByAppId(UUID.fromString(appId)).map(mapper::toDomain)

    override fun findActiveByAppId(appId: String): List<RemoteConfig> =
        jpaRepository.findByAppIdAndIsActiveTrue(UUID.fromString(appId)).map(mapper::toDomain)

    override fun save(config: RemoteConfig): RemoteConfig {
        val entity = if (config.id.isEmpty()) {
            mapper.toEntity(config)
        } else {
            jpaRepository.findByIdOrNull(UUID.fromString(config.id))?.also { mapper.updateEntity(it, config) }
                ?: mapper.toEntity(config)
        }
        return mapper.toDomain(jpaRepository.save(entity))
    }

    override fun deleteById(id: String) = jpaRepository.deleteById(UUID.fromString(id))

    override fun existsByAppIdAndConfigKey(appId: String, configKey: String): Boolean =
        jpaRepository.existsByAppIdAndConfigKey(UUID.fromString(appId), configKey)
}
