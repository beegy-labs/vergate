package com.verobee.vergate.adapters.`in`.rest.admin

import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateVersionRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateVersionRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.VersionResponse
import com.verobee.vergate.common.response.ApiResponse
import com.verobee.vergate.domain.model.AppVersion
import com.verobee.vergate.domain.service.VersionService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin - Versions")
@RestController
@RequestMapping("/api/v1/admin/apps/{appId}/versions")
class VersionAdminController(
    private val versionService: VersionService,
) {

    @GetMapping
    fun list(@PathVariable appId: String): ApiResponse<List<VersionResponse>> =
        ApiResponse.ok(versionService.findByAppId(appId).map { it.toResponse() })

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable appId: String, @Valid @RequestBody req: CreateVersionRequest): ApiResponse<VersionResponse> =
        ApiResponse.ok(
            versionService.create(appId, req.minVersion, req.latestVersion, req.forceUpdate, req.updateMessage).toResponse()
        )

    @PutMapping("/{id}")
    fun update(@PathVariable appId: String, @PathVariable id: String, @Valid @RequestBody req: UpdateVersionRequest): ApiResponse<VersionResponse> =
        ApiResponse.ok(
            versionService.update(id, req.minVersion, req.latestVersion, req.forceUpdate, req.updateMessage, req.isActive).toResponse()
        )

    private fun AppVersion.toResponse() = VersionResponse(
        id = id, appId = appId, minVersion = minVersion, latestVersion = latestVersion,
        forceUpdate = forceUpdate, updateMessage = updateMessage, isActive = isActive,
        createdAt = createdAt, updatedAt = updatedAt,
    )
}
