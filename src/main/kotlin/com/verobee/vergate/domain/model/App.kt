package com.verobee.vergate.domain.model

import java.time.OffsetDateTime

data class App(
    val id: String = "",
    val appKey: String,
    val name: String,
    val description: String? = null,
    val platform: Platform,
    val storeUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
)

enum class Platform {
    IOS, ANDROID, WEB
}
