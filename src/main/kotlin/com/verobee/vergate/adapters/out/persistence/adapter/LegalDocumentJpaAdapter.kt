package com.verobee.vergate.adapters.out.persistence.adapter

import com.verobee.vergate.adapters.out.persistence.entity.LegalDocTypeJpa
import com.verobee.vergate.adapters.out.persistence.mapper.LegalDocumentMapper
import com.verobee.vergate.adapters.out.persistence.repository.LegalDocumentJpaRepository
import com.verobee.vergate.domain.model.LegalDocType
import com.verobee.vergate.domain.model.LegalDocument
import com.verobee.vergate.ports.out.LegalDocumentRepositoryPort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class LegalDocumentJpaAdapter(
    private val repository: LegalDocumentJpaRepository,
    private val mapper: LegalDocumentMapper,
) : LegalDocumentRepositoryPort {

    override fun findActiveByAppId(appId: String) =
        repository.findByAppIdAndIsActiveTrue(UUID.fromString(appId)).map(mapper::toDomain)

    override fun findByAppId(appId: String) =
        repository.findByAppId(UUID.fromString(appId)).map(mapper::toDomain)

    override fun findByAppIdAndType(appId: String, docType: LegalDocType) =
        repository.findByAppIdAndDocType(UUID.fromString(appId), LegalDocTypeJpa.valueOf(docType.name))
            ?.let(mapper::toDomain)

    override fun findById(id: String) =
        repository.findById(UUID.fromString(id)).orElse(null)?.let(mapper::toDomain)

    override fun existsByAppIdAndDocType(appId: String, docType: LegalDocType) =
        repository.existsByAppIdAndDocType(UUID.fromString(appId), LegalDocTypeJpa.valueOf(docType.name))

    override fun save(doc: LegalDocument): LegalDocument {
        val entity = if (doc.id.isEmpty()) {
            mapper.toEntity(doc)
        } else {
            repository.findById(UUID.fromString(doc.id)).orElse(null)
                ?.also { mapper.updateEntity(it, doc) }
                ?: mapper.toEntity(doc)
        }
        return mapper.toDomain(repository.save(entity))
    }

    override fun deleteById(id: String) = repository.deleteById(UUID.fromString(id))
}
