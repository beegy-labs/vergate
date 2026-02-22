package com.verobee.vergate.adapters.out.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

enum class LegalDocTypeJpa { PRIVACY_POLICY, TERMS_OF_SERVICE }
enum class LegalContentTypeJpa { MARKDOWN, HTML }

@Entity
@Table(
    name = "legal_documents",
    uniqueConstraints = [UniqueConstraint(columnNames = ["app_id", "doc_type"])],
)
class LegalDocumentJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    val id: UUID,

    @Column(name = "app_id", nullable = false, columnDefinition = "uuid")
    val appId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 50)
    val docType: LegalDocTypeJpa,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    var contentType: LegalContentTypeJpa = LegalContentTypeJpa.MARKDOWN,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
