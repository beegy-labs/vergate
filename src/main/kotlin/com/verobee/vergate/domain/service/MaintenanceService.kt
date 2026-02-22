package com.verobee.vergate.domain.service

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.common.exception.ErrorCode
import com.verobee.vergate.domain.model.Maintenance
import com.verobee.vergate.domain.util.AppKeyGenerator
import com.verobee.vergate.ports.out.GatewayCachePort
import com.verobee.vergate.ports.out.MaintenanceRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class MaintenanceService(
    private val maintenanceRepository: MaintenanceRepositoryPort,
    private val cache: GatewayCachePort,
) {

    fun findByAppId(appId: String): List<Maintenance> = maintenanceRepository.findByAppId(appId)

    fun findById(id: String): Maintenance =
        maintenanceRepository.findById(id) ?: throw ApiException(ErrorCode.MAINTENANCE_NOT_FOUND)

    @Transactional
    fun create(appId: String, title: String, message: String?, startAt: OffsetDateTime, endAt: OffsetDateTime): Maintenance {
        val saved = maintenanceRepository.save(
            Maintenance(
                id = AppKeyGenerator.generateUuidV7().toString(),
                appId = appId, title = title, message = message, startAt = startAt, endAt = endAt,
            )
        )
        cache.evictByAppId(appId)
        return saved
    }

    @Transactional
    fun update(id: String, title: String, message: String?, startAt: OffsetDateTime, endAt: OffsetDateTime, isActive: Boolean): Maintenance {
        val existing = findById(id)
        val updated = existing.copy(
            title = title, message = message, startAt = startAt, endAt = endAt,
            isActive = isActive, updatedAt = OffsetDateTime.now(),
        )
        val saved = maintenanceRepository.save(updated)
        cache.evictByAppId(saved.appId)
        return saved
    }

    @Transactional
    fun delete(id: String) {
        val m = findById(id)
        cache.evictByAppId(m.appId)
        maintenanceRepository.deleteById(id)
    }
}
