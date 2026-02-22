package com.verobee.vergate.adapters.out.persistence.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "remote_configs")
class RemoteConfigJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    val id: UUID,

    @Column(name = "app_id", nullable = false, columnDefinition = "uuid")
    var appId: UUID,

    @Column(name = "config_key", nullable = false, length = 100)
    var configKey: String,

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    var configValue: String,

    @Column(name = "value_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var valueType: ValueTypeJpa = ValueTypeJpa.STRING,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "ab_ratio", nullable = false)
    var abRatio: Int = 100,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)

enum class ValueTypeJpa {
    STRING, NUMBER, BOOLEAN, JSON
}
