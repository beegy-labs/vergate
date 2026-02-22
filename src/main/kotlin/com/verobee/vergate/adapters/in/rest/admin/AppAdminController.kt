package com.verobee.vergate.adapters.`in`.rest.admin

import com.verobee.vergate.adapters.`in`.rest.dto.admin.AppResponse
import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateAppRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateAppRequest
import com.verobee.vergate.common.response.ApiResponse
import com.verobee.vergate.domain.model.App
import com.verobee.vergate.domain.service.AppService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin - Apps")
@RestController
@RequestMapping("/api/v1/admin/apps")
class AppAdminController(
    private val appService: AppService,
) {

    @GetMapping
    fun list(): ApiResponse<List<AppResponse>> =
        ApiResponse.ok(appService.findAll().map { it.toResponse() })

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): ApiResponse<AppResponse> =
        ApiResponse.ok(appService.findById(id).toResponse())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody req: CreateAppRequest): ApiResponse<AppResponse> =
        ApiResponse.ok(
            appService.create(req.name, req.description, req.platform, req.storeUrl).toResponse()
        )

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @Valid @RequestBody req: UpdateAppRequest): ApiResponse<AppResponse> =
        ApiResponse.ok(
            appService.update(id, req.name, req.description, req.platform, req.storeUrl, req.isActive).toResponse()
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: String) {
        appService.delete(id)
    }

    private fun App.toResponse() = AppResponse(
        id = id, appKey = appKey, name = name, description = description,
        platform = platform.name, storeUrl = storeUrl, isActive = isActive,
        createdAt = createdAt, updatedAt = updatedAt,
    )
}
