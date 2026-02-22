package com.verobee.vergate.adapters.`in`.rest.admin

import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateMaintenanceRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.MaintenanceResponse
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateMaintenanceRequest
import com.verobee.vergate.common.response.ApiResponse
import com.verobee.vergate.domain.model.Maintenance
import com.verobee.vergate.domain.service.MaintenanceService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin - Maintenances")
@RestController
@RequestMapping("/api/v1/admin/apps/{appId}/maintenances")
class MaintenanceAdminController(
    private val maintenanceService: MaintenanceService,
) {

    @GetMapping
    fun list(@PathVariable appId: String): ApiResponse<List<MaintenanceResponse>> =
        ApiResponse.ok(maintenanceService.findByAppId(appId).map { it.toResponse() })

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable appId: String, @Valid @RequestBody req: CreateMaintenanceRequest): ApiResponse<MaintenanceResponse> =
        ApiResponse.ok(
            maintenanceService.create(appId, req.title, req.message, req.startAt, req.endAt).toResponse()
        )

    @PutMapping("/{id}")
    fun update(@PathVariable appId: String, @PathVariable id: String, @Valid @RequestBody req: UpdateMaintenanceRequest): ApiResponse<MaintenanceResponse> =
        ApiResponse.ok(
            maintenanceService.update(id, req.title, req.message, req.startAt, req.endAt, req.isActive).toResponse()
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: String) {
        maintenanceService.delete(id)
    }

    private fun Maintenance.toResponse() = MaintenanceResponse(
        id = id, appId = appId, title = title, message = message,
        startAt = startAt, endAt = endAt, isActive = isActive,
        createdAt = createdAt, updatedAt = updatedAt,
    )
}
