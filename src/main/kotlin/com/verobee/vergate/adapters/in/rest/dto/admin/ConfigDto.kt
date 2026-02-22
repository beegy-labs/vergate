package com.verobee.vergate.adapters.`in`.rest.dto.admin

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class CreateConfigRequest(
    @field:NotBlank val configKey: String,
    @field:NotBlank val configValue: String,
    val valueType: String = "STRING",
    val description: String? = null,
    @field:Min(0) @field:Max(100) val abRatio: Int = 100,
)

data class UpdateConfigRequest(
    @field:NotBlank val configValue: String,
    val valueType: String = "STRING",
    val description: String? = null,
    @field:Min(0) @field:Max(100) val abRatio: Int = 100,
    val isActive: Boolean = true,
)

data class ConfigResponse(
    val id: String,
    val appId: String,
    val configKey: String,
    val configValue: String,
    val valueType: String,
    val description: String?,
    val abRatio: Int,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
