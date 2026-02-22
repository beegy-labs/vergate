package com.verobee.vergate.domain.service

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.common.exception.ErrorCode
import com.verobee.vergate.domain.model.LegalContentType
import com.verobee.vergate.domain.model.LegalDocType
import com.verobee.vergate.domain.model.LegalDocument
import com.verobee.vergate.domain.util.AppKeyGenerator
import com.verobee.vergate.ports.out.AppRepositoryPort
import com.verobee.vergate.ports.out.GatewayCachePort
import com.verobee.vergate.ports.out.LegalDocumentRepositoryPort
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class LegalDocumentService(
    private val legalDocRepository: LegalDocumentRepositoryPort,
    private val appRepository: AppRepositoryPort,
    private val cache: GatewayCachePort,
) {

    fun findByAppKeyActive(appKey: String): List<LegalDocument> {
        val app = appRepository.findByAppKey(appKey) ?: throw ApiException(ErrorCode.APP_NOT_FOUND)
        return legalDocRepository.findActiveByAppId(app.id)
    }

    fun findByAppKeyAndType(appKey: String, docType: LegalDocType): LegalDocument {
        val app = appRepository.findByAppKey(appKey) ?: throw ApiException(ErrorCode.APP_NOT_FOUND)
        return legalDocRepository.findByAppIdAndType(app.id, docType)
            ?: throw ApiException(ErrorCode.LEGAL_DOC_NOT_FOUND)
    }

    fun findByAppId(appId: String): List<LegalDocument> =
        legalDocRepository.findByAppId(appId)

    fun findById(id: String): LegalDocument =
        legalDocRepository.findById(id) ?: throw ApiException(ErrorCode.LEGAL_DOC_NOT_FOUND)

    fun create(appId: String, docType: String, title: String, content: String, contentType: String): LegalDocument {
        val type = LegalDocType.valueOf(docType)
        if (legalDocRepository.existsByAppIdAndDocType(appId, type)) {
            throw ApiException(ErrorCode.LEGAL_DOC_TYPE_DUPLICATE)
        }
        return legalDocRepository.save(
            LegalDocument(
                id = AppKeyGenerator.generateUuidV7().toString(),
                appId = appId,
                docType = type,
                title = title,
                content = content,
                contentType = LegalContentType.valueOf(contentType),
            )
        ).also { cache.evictByAppId(appId) }
    }

    fun update(id: String, title: String, content: String, contentType: String, isActive: Boolean): LegalDocument {
        val doc = findById(id)
        return legalDocRepository.save(
            doc.copy(
                title = title,
                content = content,
                contentType = LegalContentType.valueOf(contentType),
                isActive = isActive,
                updatedAt = OffsetDateTime.now(),
            )
        ).also { cache.evictByAppId(doc.appId) }
    }

    fun delete(id: String) {
        val doc = findById(id)
        legalDocRepository.deleteById(id)
        cache.evictByAppId(doc.appId)
    }
}
