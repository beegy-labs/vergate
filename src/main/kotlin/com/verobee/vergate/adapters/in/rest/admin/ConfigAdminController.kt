package com.verobee.vergate.adapters.`in`.rest.admin

import com.verobee.vergate.adapters.`in`.rest.dto.admin.ConfigResponse
import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateConfigRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateConfigRequest
import com.verobee.vergate.common.response.ApiResponse
import com.verobee.vergate.domain.model.RemoteConfig
import com.verobee.vergate.domain.service.RemoteConfigService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin - Remote Config")
@RestController
@RequestMapping("/api/v1/admin/apps/{appId}/configs")
class ConfigAdminController(
    private val configService: RemoteConfigService,
) {

    @GetMapping
    fun list(@PathVariable appId: String): ApiResponse<List<ConfigResponse>> =
        ApiResponse.ok(configService.findByAppId(appId).map { it.toResponse() })

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable appId: String, @Valid @RequestBody req: CreateConfigRequest): ApiResponse<ConfigResponse> =
        ApiResponse.ok(
            configService.create(appId, req.configKey, req.configValue, req.valueType, req.description, req.abRatio).toResponse()
        )

    @PutMapping("/{id}")
    fun update(@PathVariable appId: String, @PathVariable id: String, @Valid @RequestBody req: UpdateConfigRequest): ApiResponse<ConfigResponse> =
        ApiResponse.ok(
            configService.update(id, req.configValue, req.valueType, req.description, req.abRatio, req.isActive).toResponse()
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: String) {
        configService.delete(id)
    }

    private fun RemoteConfig.toResponse() = ConfigResponse(
        id = id, appId = appId, configKey = configKey, configValue = configValue,
        valueType = valueType.name, description = description, abRatio = abRatio,
        isActive = isActive, createdAt = createdAt, updatedAt = updatedAt,
    )
}
