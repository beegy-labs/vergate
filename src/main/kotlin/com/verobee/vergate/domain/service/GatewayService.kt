package com.verobee.vergate.domain.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.verobee.vergate.adapters.`in`.rest.dto.client.*
import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.common.exception.ErrorCode
import com.verobee.vergate.domain.model.LegalDocType
import com.verobee.vergate.ports.out.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class GatewayService(
    private val appRepository: AppRepositoryPort,
    private val versionRepository: AppVersionRepositoryPort,
    private val maintenanceRepository: MaintenanceRepositoryPort,
    private val noticeRepository: NoticeRepositoryPort,
    private val configRepository: RemoteConfigRepositoryPort,
    private val legalDocRepository: LegalDocumentRepositoryPort,
    private val cache: GatewayCachePort,
    private val objectMapper: ObjectMapper,
    @Value("\${vergate.cache.init-ttl-seconds:60}")
    private val cacheTtlSeconds: Long,
) {

    fun init(appKey: String, platform: String, appVersion: String): InitResponse {
        val cacheKey = "$appKey:$platform:$appVersion"

        // Try Valkey cache first
        cache.getInitResponse(cacheKey)?.let { cached ->
            return objectMapper.readValue(cached, InitResponse::class.java)
        }

        // Cache miss — build response from DB
        val response = buildInitResponse(appKey, platform, appVersion)

        // Write to Valkey (fire-and-forget, never blocks the response)
        cache.setInitResponse(cacheKey, objectMapper.writeValueAsString(response), cacheTtlSeconds)

        return response
    }

    private fun buildInitResponse(appKey: String, platform: String, appVersion: String): InitResponse {
        val app = appRepository.findByAppKey(appKey)
            ?: throw ApiException(ErrorCode.APP_NOT_FOUND)

        // Service termination check: if app is inactive, block access
        if (!app.isActive) {
            return InitResponse(
                service = ServiceStatus(active = false, message = "This service has been terminated."),
                update = null,
                maintenance = null,
                notices = emptyList(),
                config = emptyMap(),
                legal = emptyList(),
            )
        }

        // Version check
        val versionRule = versionRepository.findActiveByAppId(app.id)
        val updateInfo = versionRule?.let {
            UpdateInfo(
                force = it.forceUpdate || isVersionBelow(appVersion, it.minVersion),
                latestVersion = it.latestVersion,
                minVersion = it.minVersion,
                storeUrl = app.storeUrl,
                message = it.updateMessage,
            )
        }

        // Maintenance check
        val now = OffsetDateTime.now()
        val maintenance = maintenanceRepository.findActiveByAppIdAndTime(app.id, now)
        val maintenanceInfo = MaintenanceInfo(
            active = maintenance != null,
            title = maintenance?.title,
            message = maintenance?.message,
            startAt = maintenance?.startAt?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            endAt = maintenance?.endAt?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        )

        // Notices
        val notices = noticeRepository.findActiveByAppIdAndTime(app.id, now).map { n ->
            NoticeInfo(
                id = n.id,
                title = n.title,
                message = n.message,
                imageUrl = n.imageUrl,
                deepLink = n.deepLink,
                displayType = n.displayType.name,
            )
        }

        // Remote configs → flat map
        val configs = configRepository.findActiveByAppId(app.id).associate { c ->
            c.configKey to parseConfigValue(c.configValue, c.valueType.name)
        }

        // Legal document links
        val legalLinks = legalDocRepository.findActiveByAppId(app.id).map { doc ->
            LegalLinkInfo(
                type = doc.docType.name,
                title = doc.title,
                url = "/api/v1/legal/${app.appKey}/${doc.docType.toPath()}",
            )
        }

        return InitResponse(
            service = ServiceStatus(active = true),
            update = updateInfo,
            maintenance = maintenanceInfo,
            notices = notices,
            config = configs,
            legal = legalLinks,
        )
    }

    private fun isVersionBelow(current: String, min: String): Boolean {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val minParts = min.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLen = maxOf(currentParts.size, minParts.size)
        for (i in 0 until maxLen) {
            val c = currentParts.getOrElse(i) { 0 }
            val m = minParts.getOrElse(i) { 0 }
            if (c < m) return true
            if (c > m) return false
        }
        return false
    }

    private fun parseConfigValue(value: String, type: String): Any? = when (type) {
        "BOOLEAN" -> value.toBooleanStrictOrNull() ?: value
        "NUMBER" -> value.toLongOrNull() ?: value.toDoubleOrNull() ?: value
        "JSON" -> objectMapper.readValue(value, Any::class.java)
        else -> value
    }
}
