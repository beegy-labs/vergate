package com.verobee.vergate.domain.model

import java.time.OffsetDateTime

data class LegalDocument(
    val id: String = "",
    val appId: String,
    val docType: LegalDocType,
    val title: String,
    val content: String,
    val contentType: LegalContentType = LegalContentType.MARKDOWN,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
)

enum class LegalDocType {
    PRIVACY_POLICY, TERMS_OF_SERVICE;

    fun toPath(): String = when (this) {
        PRIVACY_POLICY -> "privacy-policy"
        TERMS_OF_SERVICE -> "terms"
    }
}

enum class LegalContentType {
    MARKDOWN, HTML
}
