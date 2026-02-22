package com.verobee.vergate.adapters.out.persistence.mapper

import com.verobee.vergate.adapters.out.persistence.entity.LegalContentTypeJpa
import com.verobee.vergate.adapters.out.persistence.entity.LegalDocTypeJpa
import com.verobee.vergate.adapters.out.persistence.entity.LegalDocumentJpaEntity
import com.verobee.vergate.domain.model.LegalContentType
import com.verobee.vergate.domain.model.LegalDocType
import com.verobee.vergate.domain.model.LegalDocument
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class LegalDocumentMapper {

    fun toDomain(entity: LegalDocumentJpaEntity) = LegalDocument(
        id = entity.id.toString(),
        appId = entity.appId.toString(),
        docType = LegalDocType.valueOf(entity.docType.name),
        title = entity.title,
        content = entity.content,
        contentType = LegalContentType.valueOf(entity.contentType.name),
        isActive = entity.isActive,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(domain: LegalDocument) = LegalDocumentJpaEntity(
        id = UUID.fromString(domain.id),
        appId = UUID.fromString(domain.appId),
        docType = LegalDocTypeJpa.valueOf(domain.docType.name),
        title = domain.title,
        content = domain.content,
        contentType = LegalContentTypeJpa.valueOf(domain.contentType.name),
        isActive = domain.isActive,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
    )

    fun updateEntity(entity: LegalDocumentJpaEntity, domain: LegalDocument) {
        entity.title = domain.title
        entity.content = domain.content
        entity.contentType = LegalContentTypeJpa.valueOf(domain.contentType.name)
        entity.isActive = domain.isActive
        entity.updatedAt = domain.updatedAt
    }
}
