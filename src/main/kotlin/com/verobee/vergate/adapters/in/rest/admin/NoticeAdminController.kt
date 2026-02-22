package com.verobee.vergate.adapters.`in`.rest.admin

import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateNoticeRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.NoticeResponse
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateNoticeRequest
import com.verobee.vergate.common.response.ApiResponse
import com.verobee.vergate.domain.model.Notice
import com.verobee.vergate.domain.service.NoticeService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin - Notices")
@RestController
@RequestMapping("/api/v1/admin/apps/{appId}/notices")
class NoticeAdminController(
    private val noticeService: NoticeService,
) {

    @GetMapping
    fun list(@PathVariable appId: String): ApiResponse<List<NoticeResponse>> =
        ApiResponse.ok(noticeService.findByAppId(appId).map { it.toResponse() })

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable appId: String, @Valid @RequestBody req: CreateNoticeRequest): ApiResponse<NoticeResponse> =
        ApiResponse.ok(
            noticeService.create(
                appId, req.title, req.message, req.imageUrl, req.deepLink,
                req.displayType, req.priority, req.startAt, req.endAt,
            ).toResponse()
        )

    @PutMapping("/{id}")
    fun update(@PathVariable appId: String, @PathVariable id: String, @Valid @RequestBody req: UpdateNoticeRequest): ApiResponse<NoticeResponse> =
        ApiResponse.ok(
            noticeService.update(
                id, req.title, req.message, req.imageUrl, req.deepLink,
                req.displayType, req.priority, req.startAt, req.endAt, req.isActive,
            ).toResponse()
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: String) {
        noticeService.delete(id)
    }

    private fun Notice.toResponse() = NoticeResponse(
        id = id, appId = appId, title = title, message = message,
        imageUrl = imageUrl, deepLink = deepLink, displayType = displayType.name,
        priority = priority, startAt = startAt, endAt = endAt, isActive = isActive,
        createdAt = createdAt, updatedAt = updatedAt,
    )
}
