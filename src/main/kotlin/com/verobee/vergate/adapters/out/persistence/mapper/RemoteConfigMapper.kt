package com.verobee.vergate.adapters.out.persistence.mapper

import com.verobee.vergate.adapters.out.persistence.entity.RemoteConfigJpaEntity
import com.verobee.vergate.adapters.out.persistence.entity.ValueTypeJpa
import com.verobee.vergate.domain.model.RemoteConfig
import com.verobee.vergate.domain.model.ValueType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RemoteConfigMapper {

    fun toDomain(entity: RemoteConfigJpaEntity) = RemoteConfig(
        id = entity.id.toString(),
        appId = entity.appId.toString(),
        configKey = entity.configKey,
        configValue = entity.configValue,
        valueType = ValueType.valueOf(entity.valueType.name),
        description = entity.description,
        abRatio = entity.abRatio,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(domain: RemoteConfig) = RemoteConfigJpaEntity(
        id = UUID.fromString(domain.id),
        appId = UUID.fromString(domain.appId),
        configKey = domain.configKey,
        configValue = domain.configValue,
        valueType = ValueTypeJpa.valueOf(domain.valueType.name),
        description = domain.description,
        abRatio = domain.abRatio,
        isActive = domain.isActive,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
    )

    fun updateEntity(entity: RemoteConfigJpaEntity, domain: RemoteConfig) {
        entity.configValue = domain.configValue
        entity.valueType = ValueTypeJpa.valueOf(domain.valueType.name)
        entity.description = domain.description
        entity.abRatio = domain.abRatio
        entity.isActive = domain.isActive
        entity.updatedAt = domain.updatedAt
    }
}
