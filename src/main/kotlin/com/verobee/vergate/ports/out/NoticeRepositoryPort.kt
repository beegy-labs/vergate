package com.verobee.vergate.ports.out

import com.verobee.vergate.domain.model.Notice
import java.time.OffsetDateTime

interface NoticeRepositoryPort {
    fun findById(id: String): Notice?
    fun findByAppId(appId: String): List<Notice>
    fun findActiveByAppIdAndTime(appId: String, now: OffsetDateTime): List<Notice>
    fun save(notice: Notice): Notice
    fun deleteById(id: String)
}
