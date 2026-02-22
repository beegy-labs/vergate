package com.verobee.vergate.adapters.`in`.rest.dto.admin

import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class CreateAppRequest(
    @field:NotBlank val name: String,
    val description: String? = null,
    @field:NotBlank val platform: String,
    val storeUrl: String? = null,
)

data class UpdateAppRequest(
    @field:NotBlank val name: String,
    val description: String? = null,
    @field:NotBlank val platform: String,
    val storeUrl: String? = null,
    val isActive: Boolean = true,
)

data class AppResponse(
    val id: String,
    val appKey: String,
    val name: String,
    val description: String?,
    val platform: String,
    val storeUrl: String?,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
