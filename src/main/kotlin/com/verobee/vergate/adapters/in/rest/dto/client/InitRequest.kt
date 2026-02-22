package com.verobee.vergate.adapters.`in`.rest.dto.client

import jakarta.validation.constraints.NotBlank

data class InitRequest(
    @field:NotBlank
    val appKey: String,

    @field:NotBlank
    val platform: String,

    @field:NotBlank
    val appVersion: String,

    val osVersion: String? = null,
)
