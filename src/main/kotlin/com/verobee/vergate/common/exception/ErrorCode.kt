package com.verobee.vergate.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    // App
    APP_NOT_FOUND(HttpStatus.NOT_FOUND, "APP_001", "App not found"),
    APP_KEY_DUPLICATE(HttpStatus.CONFLICT, "APP_002", "App key already exists"),
    APP_INACTIVE(HttpStatus.FORBIDDEN, "APP_003", "App is no longer available"),

    // Version
    VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "VER_001", "Version rule not found"),

    // Maintenance
    MAINTENANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "MNT_001", "Maintenance not found"),

    // Notice
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NTC_001", "Notice not found"),

    // Config
    CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "CFG_001", "Config not found"),
    CONFIG_KEY_DUPLICATE(HttpStatus.CONFLICT, "CFG_002", "Config key already exists"),

    // Legal
    LEGAL_DOC_NOT_FOUND(HttpStatus.NOT_FOUND, "LGL_001", "Legal document not found"),
    LEGAL_DOC_TYPE_DUPLICATE(HttpStatus.CONFLICT, "LGL_002", "Legal document type already exists for this app"),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "CMN_001", "Invalid input"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CMN_999", "Internal server error"),
}
