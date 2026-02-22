package com.verobee.vergate.domain.service

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.common.exception.ErrorCode
import com.verobee.vergate.domain.model.AppVersion
import com.verobee.vergate.domain.util.AppKeyGenerator
import com.verobee.vergate.ports.out.AppVersionRepositoryPort
import com.verobee.vergate.ports.out.GatewayCachePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class VersionService(
    private val versionRepository: AppVersionRepositoryPort,
    private val cache: GatewayCachePort,
) {

    fun findByAppId(appId: String): List<AppVersion> = versionRepository.findByAppId(appId)

    fun findById(id: String): AppVersion =
        versionRepository.findById(id) ?: throw ApiException(ErrorCode.VERSION_NOT_FOUND)

    @Transactional
    fun create(appId: String, minVersion: String, latestVersion: String, forceUpdate: Boolean, updateMessage: String?): AppVersion {
        val saved = versionRepository.save(
            AppVersion(
                id = AppKeyGenerator.generateUuidV7().toString(),
                appId = appId,
                minVersion = minVersion,
                latestVersion = latestVersion,
                forceUpdate = forceUpdate,
                updateMessage = updateMessage,
            )
        )
        cache.evictByAppId(appId)
        return saved
    }

    @Transactional
    fun update(id: String, minVersion: String, latestVersion: String, forceUpdate: Boolean, updateMessage: String?, isActive: Boolean): AppVersion {
        val existing = findById(id)
        val updated = existing.copy(
            minVersion = minVersion,
            latestVersion = latestVersion,
            forceUpdate = forceUpdate,
            updateMessage = updateMessage,
            isActive = isActive,
            updatedAt = OffsetDateTime.now(),
        )
        val saved = versionRepository.save(updated)
        cache.evictByAppId(saved.appId)
        return saved
    }
}
