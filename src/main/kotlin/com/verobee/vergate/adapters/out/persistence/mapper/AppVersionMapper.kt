package com.verobee.vergate.adapters.out.persistence.mapper

import com.verobee.vergate.adapters.out.persistence.entity.AppVersionJpaEntity
import com.verobee.vergate.domain.model.AppVersion
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AppVersionMapper {

    fun toDomain(entity: AppVersionJpaEntity) = AppVersion(
        id = entity.id.toString(),
        appId = entity.appId.toString(),
        minVersion = entity.minVersion,
        latestVersion = entity.latestVersion,
        forceUpdate = entity.forceUpdate,
        updateMessage = entity.updateMessage,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(domain: AppVersion) = AppVersionJpaEntity(
        id = UUID.fromString(domain.id),
        appId = UUID.fromString(domain.appId),
        minVersion = domain.minVersion,
        latestVersion = domain.latestVersion,
        forceUpdate = domain.forceUpdate,
        updateMessage = domain.updateMessage,
        isActive = domain.isActive,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
    )

    fun updateEntity(entity: AppVersionJpaEntity, domain: AppVersion) {
        entity.minVersion = domain.minVersion
        entity.latestVersion = domain.latestVersion
        entity.forceUpdate = domain.forceUpdate
        entity.updateMessage = domain.updateMessage
        entity.isActive = domain.isActive
        entity.updatedAt = domain.updatedAt
    }
}
