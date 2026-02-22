package com.verobee.vergate.domain.exception

class DomainException(
    val errorCode: String,
    override val message: String,
) : RuntimeException(message)
