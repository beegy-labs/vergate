package com.verobee.vergate.adapters.out.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "notices")
class NoticeJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    val id: UUID,

    @Column(name = "app_id", nullable = false, columnDefinition = "uuid")
    var appId: UUID,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var message: String? = null,

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,

    @Column(name = "deep_link", length = 500)
    var deepLink: String? = null,

    @Column(name = "display_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var displayType: DisplayTypeJpa = DisplayTypeJpa.ONCE,

    @Column(nullable = false)
    var priority: Int = 0,

    @Column(name = "start_at")
    var startAt: OffsetDateTime? = null,

    @Column(name = "end_at")
    var endAt: OffsetDateTime? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)

enum class DisplayTypeJpa {
    ONCE, DAILY, ALWAYS
}
