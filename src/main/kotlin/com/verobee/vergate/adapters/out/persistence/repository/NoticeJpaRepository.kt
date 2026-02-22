package com.verobee.vergate.adapters.out.persistence.repository

import com.verobee.vergate.adapters.out.persistence.entity.NoticeJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime
import java.util.UUID

interface NoticeJpaRepository : JpaRepository<NoticeJpaEntity, UUID> {
    fun findByAppId(appId: UUID): List<NoticeJpaEntity>

    @Query(
        "SELECT n FROM NoticeJpaEntity n WHERE n.appId = :appId AND n.isActive = true " +
            "AND (n.startAt IS NULL OR n.startAt <= :now) " +
            "AND (n.endAt IS NULL OR n.endAt >= :now) " +
            "ORDER BY n.priority DESC"
    )
    fun findActiveByAppIdAndTime(appId: UUID, now: OffsetDateTime): List<NoticeJpaEntity>
}
