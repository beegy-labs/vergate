package com.verobee.vergate.adapters.`in`.rest.client

import com.verobee.vergate.adapters.`in`.rest.dto.client.InitResponse
import com.verobee.vergate.common.response.ApiResponse
import com.verobee.vergate.domain.service.GatewayService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Client Gateway", description = "Client app init API")
@RestController
@RequestMapping("/api/v1")
class GatewayController(
    private val gatewayService: GatewayService,
) {

    @Operation(summary = "App initialization", description = "Returns version, maintenance, notices, and config for the client app")
    @GetMapping("/init")
    fun init(
        @RequestParam("app_key") appKey: String,
        @RequestParam platform: String,
        @RequestParam("app_version") appVersion: String,
        @RequestParam("os_version", required = false) osVersion: String?,
    ): ApiResponse<InitResponse> =
        ApiResponse.ok(gatewayService.init(appKey, platform, appVersion))
}
