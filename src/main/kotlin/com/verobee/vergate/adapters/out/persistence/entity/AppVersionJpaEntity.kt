package com.verobee.vergate.adapters.out.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "app_versions")
class AppVersionJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    val id: UUID,

    @Column(name = "app_id", nullable = false, columnDefinition = "uuid")
    var appId: UUID,

    @Column(name = "min_version", nullable = false, length = 20)
    var minVersion: String,

    @Column(name = "latest_version", nullable = false, length = 20)
    var latestVersion: String,

    @Column(name = "force_update", nullable = false)
    var forceUpdate: Boolean = false,

    @Column(name = "update_message", columnDefinition = "TEXT")
    var updateMessage: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
