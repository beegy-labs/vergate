package com.verobee.vergate.domain.service

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.common.exception.ErrorCode
import com.verobee.vergate.domain.model.App
import com.verobee.vergate.domain.model.Platform
import com.verobee.vergate.domain.util.AppKeyGenerator
import com.verobee.vergate.ports.out.AppRepositoryPort
import com.verobee.vergate.ports.out.GatewayCachePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class AppService(
    private val appRepository: AppRepositoryPort,
    private val cache: GatewayCachePort,
) {

    fun findAll(): List<App> = appRepository.findAll()

    fun findById(id: String): App =
        appRepository.findById(id) ?: throw ApiException(ErrorCode.APP_NOT_FOUND)

    @Transactional
    fun create(name: String, description: String?, platform: String, storeUrl: String?): App {
        val (uuid, appKey) = AppKeyGenerator.generate()
        return appRepository.save(
            App(
                id = uuid.toString(),
                appKey = appKey,
                name = name,
                description = description,
                platform = Platform.valueOf(platform.uppercase()),
                storeUrl = storeUrl,
            )
        )
    }

    @Transactional
    fun update(id: String, name: String, description: String?, platform: String, storeUrl: String?, isActive: Boolean): App {
        val existing = findById(id)
        val updated = existing.copy(
            name = name,
            description = description,
            platform = Platform.valueOf(platform.uppercase()),
            storeUrl = storeUrl,
            isActive = isActive,
            updatedAt = OffsetDateTime.now(),
        )
        val saved = appRepository.save(updated)
        cache.evictByAppKey(saved.appKey)
        return saved
    }

    @Transactional
    fun delete(id: String) {
        val app = findById(id)
        cache.evictByAppKey(app.appKey)
        appRepository.deleteById(id)
    }
}
