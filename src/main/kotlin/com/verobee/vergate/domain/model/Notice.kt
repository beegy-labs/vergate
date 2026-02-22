package com.verobee.vergate.domain.model

import java.time.OffsetDateTime

data class Notice(
    val id: String = "",
    val appId: String,
    val title: String,
    val message: String? = null,
    val imageUrl: String? = null,
    val deepLink: String? = null,
    val displayType: DisplayType = DisplayType.ONCE,
    val priority: Int = 0,
    val startAt: OffsetDateTime? = null,
    val endAt: OffsetDateTime? = null,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
)

enum class DisplayType {
    ONCE, DAILY, ALWAYS
}
