package com.verobee.vergate.adapters.`in`.rest.dto.admin

import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class CreateLegalDocRequest(
    @field:NotBlank val docType: String,      // PRIVACY_POLICY | TERMS_OF_SERVICE
    @field:NotBlank val title: String,
    @field:NotBlank val content: String,
    val contentType: String = "MARKDOWN",     // MARKDOWN | HTML
)

data class UpdateLegalDocRequest(
    @field:NotBlank val title: String,
    @field:NotBlank val content: String,
    val contentType: String = "MARKDOWN",
    val isActive: Boolean = true,
)

data class LegalDocResponse(
    val id: String,
    val appId: String,
    val docType: String,
    val title: String,
    val content: String,
    val contentType: String,
    val isActive: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
