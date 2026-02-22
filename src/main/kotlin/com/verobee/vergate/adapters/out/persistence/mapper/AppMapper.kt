package com.verobee.vergate.adapters.out.persistence.mapper

import com.verobee.vergate.adapters.out.persistence.entity.AppJpaEntity
import com.verobee.vergate.adapters.out.persistence.entity.PlatformJpa
import com.verobee.vergate.domain.model.App
import com.verobee.vergate.domain.model.Platform
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AppMapper {

    fun toDomain(entity: AppJpaEntity) = App(
        id = entity.id.toString(),
        appKey = entity.appKey,
        name = entity.name,
        description = entity.description,
        platform = Platform.valueOf(entity.platform.name),
        storeUrl = entity.storeUrl,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(domain: App) = AppJpaEntity(
        id = UUID.fromString(domain.id),
        appKey = domain.appKey,
        name = domain.name,
        description = domain.description,
        platform = PlatformJpa.valueOf(domain.platform.name),
        storeUrl = domain.storeUrl,
        isActive = domain.isActive,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
    )

    fun updateEntity(entity: AppJpaEntity, domain: App) {
        entity.name = domain.name
        entity.description = domain.description
        entity.platform = PlatformJpa.valueOf(domain.platform.name)
        entity.storeUrl = domain.storeUrl
        entity.isActive = domain.isActive
        entity.updatedAt = domain.updatedAt
    }
}
