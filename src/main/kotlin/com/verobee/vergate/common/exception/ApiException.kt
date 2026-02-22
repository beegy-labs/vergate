package com.verobee.vergate.common.exception

class ApiException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)
