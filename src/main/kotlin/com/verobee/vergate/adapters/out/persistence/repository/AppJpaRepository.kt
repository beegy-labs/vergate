package com.verobee.vergate.adapters.out.persistence.repository

import com.verobee.vergate.adapters.out.persistence.entity.AppJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppJpaRepository : JpaRepository<AppJpaEntity, UUID> {
    fun findByAppKey(appKey: String): AppJpaEntity?
    fun existsByAppKey(appKey: String): Boolean
}
