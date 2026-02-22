package com.verobee.vergate.domain.model

import java.time.OffsetDateTime

data class Maintenance(
    val id: String = "",
    val appId: String,
    val title: String,
    val message: String? = null,
    val startAt: OffsetDateTime,
    val endAt: OffsetDateTime,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
