package com.verobee.vergate.domain.model

import java.time.OffsetDateTime

data class AppVersion(
    val id: String = "",
    val appId: String,
    val minVersion: String,
    val latestVersion: String,
    val forceUpdate: Boolean = false,
    val updateMessage: String? = null,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
