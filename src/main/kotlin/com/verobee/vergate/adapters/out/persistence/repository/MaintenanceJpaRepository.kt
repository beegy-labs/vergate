package com.verobee.vergate.adapters.out.persistence.repository

import com.verobee.vergate.adapters.out.persistence.entity.MaintenanceJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime
import java.util.UUID

interface MaintenanceJpaRepository : JpaRepository<MaintenanceJpaEntity, UUID> {
    fun findByAppId(appId: UUID): List<MaintenanceJpaEntity>

    @Query(
        "SELECT m FROM MaintenanceJpaEntity m WHERE m.appId = :appId AND m.isActive = true " +
            "AND m.startAt <= :now AND m.endAt >= :now ORDER BY m.startAt DESC"
    )
    fun findActiveByAppIdAndTime(appId: UUID, now: OffsetDateTime): List<MaintenanceJpaEntity>
}
