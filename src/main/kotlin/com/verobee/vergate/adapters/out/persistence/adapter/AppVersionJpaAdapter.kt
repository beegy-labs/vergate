package com.verobee.vergate.adapters.out.persistence.adapter

import com.verobee.vergate.adapters.out.persistence.mapper.AppVersionMapper
import com.verobee.vergate.adapters.out.persistence.repository.AppVersionJpaRepository
import com.verobee.vergate.domain.model.AppVersion
import com.verobee.vergate.ports.out.AppVersionRepositoryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AppVersionJpaAdapter(
    private val jpaRepository: AppVersionJpaRepository,
    private val mapper: AppVersionMapper,
) : AppVersionRepositoryPort {

    override fun findById(id: String): AppVersion? =
        jpaRepository.findByIdOrNull(UUID.fromString(id))?.let(mapper::toDomain)

    override fun findByAppId(appId: String): List<AppVersion> =
        jpaRepository.findByAppId(UUID.fromString(appId)).map(mapper::toDomain)

    override fun findActiveByAppId(appId: String): AppVersion? =
        jpaRepository.findFirstByAppIdAndIsActiveTrue(UUID.fromString(appId))?.let(mapper::toDomain)

    override fun save(version: AppVersion): AppVersion {
        val entity = if (version.id.isEmpty()) {
            mapper.toEntity(version)
        } else {
            jpaRepository.findByIdOrNull(UUID.fromString(version.id))?.also { mapper.updateEntity(it, version) }
                ?: mapper.toEntity(version)
        }
        return mapper.toDomain(jpaRepository.save(entity))
    }

    override fun deleteById(id: String) = jpaRepository.deleteById(UUID.fromString(id))
}
