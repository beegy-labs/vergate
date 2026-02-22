package com.verobee.vergate.adapters.`in`.rest.dto.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InitResponse(
    val service: ServiceStatus,
    val update: UpdateInfo?,
    val maintenance: MaintenanceInfo?,
    val notices: List<NoticeInfo>,
    val config: Map<String, Any?>,
    val legal: List<LegalLinkInfo>,
)

data class ServiceStatus(
    val active: Boolean,
    val message: String? = null,
)

data class UpdateInfo(
    val force: Boolean,
    @JsonProperty("latest_version")
    val latestVersion: String,
    @JsonProperty("min_version")
    val minVersion: String,
    @JsonProperty("store_url")
    val storeUrl: String?,
    val message: String? = null,
)

data class MaintenanceInfo(
    val active: Boolean,
    val title: String? = null,
    val message: String? = null,
    @JsonProperty("start_at")
    val startAt: String? = null,
    @JsonProperty("end_at")
    val endAt: String? = null,
)

data class NoticeInfo(
    val id: String,
    val title: String,
    val message: String?,
    @JsonProperty("image_url")
    val imageUrl: String?,
    @JsonProperty("deep_link")
    val deepLink: String?,
    @JsonProperty("display_type")
    val displayType: String,
)

data class LegalLinkInfo(
    val type: String,
    val title: String,
    val url: String,
)
