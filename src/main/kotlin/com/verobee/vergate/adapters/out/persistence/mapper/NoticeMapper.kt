package com.verobee.vergate.adapters.out.persistence.mapper

import com.verobee.vergate.adapters.out.persistence.entity.DisplayTypeJpa
import com.verobee.vergate.adapters.out.persistence.entity.NoticeJpaEntity
import com.verobee.vergate.domain.model.DisplayType
import com.verobee.vergate.domain.model.Notice
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class NoticeMapper {

    fun toDomain(entity: NoticeJpaEntity) = Notice(
        id = entity.id.toString(),
        appId = entity.appId.toString(),
        title = entity.title,
        message = entity.message,
        imageUrl = entity.imageUrl,
        deepLink = entity.deepLink,
        displayType = DisplayType.valueOf(entity.displayType.name),
        priority = entity.priority,
        startAt = entity.startAt,
        endAt = entity.endAt,
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(domain: Notice) = NoticeJpaEntity(
        id = UUID.fromString(domain.id),
        appId = UUID.fromString(domain.appId),
        title = domain.title,
        message = domain.message,
        imageUrl = domain.imageUrl,
        deepLink = domain.deepLink,
        displayType = DisplayTypeJpa.valueOf(domain.displayType.name),
        priority = domain.priority,
        startAt = domain.startAt,
        endAt = domain.endAt,
        isActive = domain.isActive,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
    )

    fun updateEntity(entity: NoticeJpaEntity, domain: Notice) {
        entity.title = domain.title
        entity.message = domain.message
        entity.imageUrl = domain.imageUrl
        entity.deepLink = domain.deepLink
        entity.displayType = DisplayTypeJpa.valueOf(domain.displayType.name)
        entity.priority = domain.priority
        entity.startAt = domain.startAt
        entity.endAt = domain.endAt
        entity.isActive = domain.isActive
        entity.updatedAt = domain.updatedAt
    }
}
