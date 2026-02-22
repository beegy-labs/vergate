package com.verobee.vergate.adapters.`in`.rest.dto.admin

import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class CreateNoticeRequest(
    @field:NotBlank val title: String,
    val message: String? = null,
    val imageUrl: String? = null,
    val deepLink: String? = null,
    val displayType: String = "ONCE",
    val priority: Int = 0,
    val startAt: OffsetDateTime? = null,
    val endAt: OffsetDateTime? = null,
)

data class UpdateNoticeRequest(
    @field:NotBlank val title: String,
    val message: String? = null,
    val imageUrl: String? = null,
    val deepLink: String? = null,
    val displayType: String = "ONCE",
    val priority: Int = 0,
    val startAt: OffsetDateTime? = null,
    val endAt: OffsetDateTime? = null,
    val isActive: Boolean = true,
)

data class NoticeResponse(
    val id: String,
    val appId: String,
    val title: String,
    val message: String?,
    val imageUrl: String?,
    val deepLink: String?,
    val displayType: String,
    val priority: Int,
    val startAt: OffsetDateTime?,
    val endAt: OffsetDateTime?,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
