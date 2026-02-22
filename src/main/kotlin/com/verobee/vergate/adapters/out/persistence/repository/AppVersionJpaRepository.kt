package com.verobee.vergate.adapters.out.persistence.repository

import com.verobee.vergate.adapters.out.persistence.entity.AppVersionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppVersionJpaRepository : JpaRepository<AppVersionJpaEntity, UUID> {
    fun findByAppId(appId: UUID): List<AppVersionJpaEntity>
    fun findFirstByAppIdAndIsActiveTrue(appId: UUID): AppVersionJpaEntity?
}
