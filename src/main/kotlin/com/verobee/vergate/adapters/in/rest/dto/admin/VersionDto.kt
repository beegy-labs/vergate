package com.verobee.vergate.adapters.`in`.rest.dto.admin

import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class CreateVersionRequest(
    @field:NotBlank val minVersion: String,
    @field:NotBlank val latestVersion: String,
    val forceUpdate: Boolean = false,
    val updateMessage: String? = null,
)

data class UpdateVersionRequest(
    @field:NotBlank val minVersion: String,
    @field:NotBlank val latestVersion: String,
    val forceUpdate: Boolean = false,
    val updateMessage: String? = null,
    val isActive: Boolean = true,
)

data class VersionResponse(
    val id: String,
    val appId: String,
    val minVersion: String,
    val latestVersion: String,
    val forceUpdate: Boolean,
    val updateMessage: String?,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
