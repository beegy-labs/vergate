package com.verobee.vergate.adapters.out.persistence.adapter

import com.verobee.vergate.adapters.out.persistence.mapper.MaintenanceMapper
import com.verobee.vergate.adapters.out.persistence.repository.MaintenanceJpaRepository
import com.verobee.vergate.domain.model.Maintenance
import com.verobee.vergate.ports.out.MaintenanceRepositoryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class MaintenanceJpaAdapter(
    private val jpaRepository: MaintenanceJpaRepository,
    private val mapper: MaintenanceMapper,
) : MaintenanceRepositoryPort {

    override fun findById(id: String): Maintenance? =
        jpaRepository.findByIdOrNull(UUID.fromString(id))?.let(mapper::toDomain)

    override fun findByAppId(appId: String): List<Maintenance> =
        jpaRepository.findByAppId(UUID.fromString(appId)).map(mapper::toDomain)

    override fun findActiveByAppIdAndTime(appId: String, now: OffsetDateTime): Maintenance? =
        jpaRepository.findActiveByAppIdAndTime(UUID.fromString(appId), now).firstOrNull()?.let(mapper::toDomain)

    override fun save(maintenance: Maintenance): Maintenance {
        val entity = if (maintenance.id.isEmpty()) {
            mapper.toEntity(maintenance)
        } else {
            jpaRepository.findByIdOrNull(UUID.fromString(maintenance.id))?.also { mapper.updateEntity(it, maintenance) }
                ?: mapper.toEntity(maintenance)
        }
        return mapper.toDomain(jpaRepository.save(entity))
    }

    override fun deleteById(id: String) = jpaRepository.deleteById(UUID.fromString(id))
}
