package com.verobee.vergate.adapters.out.persistence.mapper

import com.verobee.vergate.adapters.out.persistence.entity.MaintenanceJpaEntity
import com.verobee.vergate.domain.model.Maintenance
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MaintenanceMapper {

    fun toDomain(entity: MaintenanceJpaEntity) = Maintenance(
        id = entity.id.toString(),
        appId = entity.appId.toString(),
        title = entity.title,
        message = entity.message,
        startAt = entity.startAt,
        endAt = entity.endAt,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(domain: Maintenance) = MaintenanceJpaEntity(
        id = UUID.fromString(domain.id),
        appId = UUID.fromString(domain.appId),
        title = domain.title,
        message = domain.message,
        startAt = domain.startAt,
        endAt = domain.endAt,
        isActive = domain.isActive,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
    )

    fun updateEntity(entity: MaintenanceJpaEntity, domain: Maintenance) {
        entity.title = domain.title
        entity.message = domain.message
        entity.startAt = domain.startAt
        entity.endAt = domain.endAt
        entity.isActive = domain.isActive
        entity.updatedAt = domain.updatedAt
    }
}
