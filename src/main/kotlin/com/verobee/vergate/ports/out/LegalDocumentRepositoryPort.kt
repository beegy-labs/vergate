package com.verobee.vergate.ports.out

import com.verobee.vergate.domain.model.LegalDocument
import com.verobee.vergate.domain.model.LegalDocType

interface LegalDocumentRepositoryPort {
    fun findActiveByAppId(appId: String): List<LegalDocument>
    fun findByAppId(appId: String): List<LegalDocument>
    fun findByAppIdAndType(appId: String, docType: LegalDocType): LegalDocument?
    fun findById(id: String): LegalDocument?
    fun existsByAppIdAndDocType(appId: String, docType: LegalDocType): Boolean
    fun save(doc: LegalDocument): LegalDocument
    fun deleteById(id: String)
}
