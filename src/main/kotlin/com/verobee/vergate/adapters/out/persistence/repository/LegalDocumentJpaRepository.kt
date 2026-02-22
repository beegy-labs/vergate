package com.verobee.vergate.adapters.out.persistence.repository

import com.verobee.vergate.adapters.out.persistence.entity.LegalDocTypeJpa
import com.verobee.vergate.adapters.out.persistence.entity.LegalDocumentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LegalDocumentJpaRepository : JpaRepository<LegalDocumentJpaEntity, UUID> {
    fun findByAppIdAndIsActiveTrue(appId: UUID): List<LegalDocumentJpaEntity>
    fun findByAppId(appId: UUID): List<LegalDocumentJpaEntity>
    fun findByAppIdAndDocType(appId: UUID, docType: LegalDocTypeJpa): LegalDocumentJpaEntity?
    fun existsByAppIdAndDocType(appId: UUID, docType: LegalDocTypeJpa): Boolean
}
