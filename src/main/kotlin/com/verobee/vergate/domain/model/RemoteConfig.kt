package com.verobee.vergate.domain.model

import java.time.OffsetDateTime

data class RemoteConfig(
    val id: String = "",
    val appId: String,
    val configKey: String,
    val configValue: String,
    val valueType: ValueType = ValueType.STRING,
    val description: String? = null,
    val abRatio: Int = 100,
    val isActive: Boolean = true,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
)

enum class ValueType {
    STRING, NUMBER, BOOLEAN, JSON
}
