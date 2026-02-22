package com.verobee.vergate.adapters.out.persistence.repository

import com.verobee.vergate.adapters.out.persistence.entity.RemoteConfigJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RemoteConfigJpaRepository : JpaRepository<RemoteConfigJpaEntity, UUID> {
    fun findByAppId(appId: UUID): List<RemoteConfigJpaEntity>
    fun findByAppIdAndIsActiveTrue(appId: UUID): List<RemoteConfigJpaEntity>
    fun existsByAppIdAndConfigKey(appId: UUID, configKey: String): Boolean
}
