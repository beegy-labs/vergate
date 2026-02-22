package com.verobee.vergate.domain.service

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.common.exception.ErrorCode
import com.verobee.vergate.domain.model.DisplayType
import com.verobee.vergate.domain.model.Notice
import com.verobee.vergate.domain.util.AppKeyGenerator
import com.verobee.vergate.ports.out.GatewayCachePort
import com.verobee.vergate.ports.out.NoticeRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class NoticeService(
    private val noticeRepository: NoticeRepositoryPort,
    private val cache: GatewayCachePort,
) {

    fun findByAppId(appId: String): List<Notice> = noticeRepository.findByAppId(appId)

    fun findById(id: String): Notice =
        noticeRepository.findById(id) ?: throw ApiException(ErrorCode.NOTICE_NOT_FOUND)

    @Transactional
    fun create(
        appId: String, title: String, message: String?, imageUrl: String?, deepLink: String?,
        displayType: String, priority: Int, startAt: OffsetDateTime?, endAt: OffsetDateTime?,
    ): Notice {
        val saved = noticeRepository.save(
            Notice(
                id = AppKeyGenerator.generateUuidV7().toString(),
                appId = appId, title = title, message = message, imageUrl = imageUrl, deepLink = deepLink,
                displayType = DisplayType.valueOf(displayType.uppercase()), priority = priority,
                startAt = startAt, endAt = endAt,
            )
        )
        cache.evictByAppId(appId)
        return saved
    }

    @Transactional
    fun update(
        id: String, title: String, message: String?, imageUrl: String?, deepLink: String?,
        displayType: String, priority: Int, startAt: OffsetDateTime?, endAt: OffsetDateTime?, isActive: Boolean,
    ): Notice {
        val existing = findById(id)
        val updated = existing.copy(
            title = title, message = message, imageUrl = imageUrl, deepLink = deepLink,
            displayType = DisplayType.valueOf(displayType.uppercase()), priority = priority,
            startAt = startAt, endAt = endAt, isActive = isActive, updatedAt = OffsetDateTime.now(),
        )
        val saved = noticeRepository.save(updated)
        cache.evictByAppId(saved.appId)
        return saved
    }

    @Transactional
    fun delete(id: String) {
        val n = findById(id)
        cache.evictByAppId(n.appId)
        noticeRepository.deleteById(id)
    }
}
