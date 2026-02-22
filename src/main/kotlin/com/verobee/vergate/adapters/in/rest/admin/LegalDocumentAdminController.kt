package com.verobee.vergate.adapters.`in`.rest.admin

import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateLegalDocRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.LegalDocResponse
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateLegalDocRequest
import com.verobee.vergate.common.response.ApiResponse
import com.verobee.vergate.domain.model.LegalDocument
import com.verobee.vergate.domain.service.LegalDocumentService
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin - Legal Documents")
@RestController
@RequestMapping("/api/v1/admin/apps/{appId}/legal-docs")
class LegalDocumentAdminController(
    private val legalDocService: LegalDocumentService,
) {

    @GetMapping
    fun list(@PathVariable appId: String): ApiResponse<List<LegalDocResponse>> =
        ApiResponse.ok(legalDocService.findByAppId(appId).map { it.toResponse() })

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable appId: String, @Valid @RequestBody req: CreateLegalDocRequest): ApiResponse<LegalDocResponse> =
        ApiResponse.ok(
            legalDocService.create(appId, req.docType, req.title, req.content, req.contentType).toResponse()
        )

    @PutMapping("/{id}")
    fun update(
        @PathVariable appId: String,
        @PathVariable id: String,
        @Valid @RequestBody req: UpdateLegalDocRequest,
    ): ApiResponse<LegalDocResponse> =
        ApiResponse.ok(
            legalDocService.update(id, req.title, req.content, req.contentType, req.isActive).toResponse()
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: String) {
        legalDocService.delete(id)
    }

    private fun LegalDocument.toResponse() = LegalDocResponse(
        id = id, appId = appId, docType = docType.name, title = title,
        content = content, contentType = contentType.name, isActive = isActive,
        createdAt = createdAt, updatedAt = updatedAt,
    )
}
