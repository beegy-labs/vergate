package com.verobee.vergate.adapters.out.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "maintenances")
class MaintenanceJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    val id: UUID,

    @Column(name = "app_id", nullable = false, columnDefinition = "uuid")
    var appId: UUID,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var message: String? = null,

    @Column(name = "start_at", nullable = false)
    var startAt: OffsetDateTime,

    @Column(name = "end_at", nullable = false)
    var endAt: OffsetDateTime,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
