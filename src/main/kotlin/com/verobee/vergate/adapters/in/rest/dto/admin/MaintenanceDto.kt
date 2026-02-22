package com.verobee.vergate.adapters.`in`.rest.dto.admin

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class CreateMaintenanceRequest(
    @field:NotBlank val title: String,
    val message: String? = null,
    @field:NotNull val startAt: OffsetDateTime,
    @field:NotNull val endAt: OffsetDateTime,
)

data class UpdateMaintenanceRequest(
    @field:NotBlank val title: String,
    val message: String? = null,
    @field:NotNull val startAt: OffsetDateTime,
    @field:NotNull val endAt: OffsetDateTime,
    val isActive: Boolean = true,
)

data class MaintenanceResponse(
    val id: String,
    val appId: String,
    val title: String,
    val message: String?,
    val startAt: OffsetDateTime,
    val endAt: OffsetDateTime,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
