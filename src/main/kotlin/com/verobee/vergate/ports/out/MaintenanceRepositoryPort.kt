package com.verobee.vergate.ports.out

import com.verobee.vergate.domain.model.Maintenance
import java.time.OffsetDateTime

interface MaintenanceRepositoryPort {
    fun findById(id: String): Maintenance?
    fun findByAppId(appId: String): List<Maintenance>
    fun findActiveByAppIdAndTime(appId: String, now: OffsetDateTime): Maintenance?
    fun save(maintenance: Maintenance): Maintenance
    fun deleteById(id: String)
}
