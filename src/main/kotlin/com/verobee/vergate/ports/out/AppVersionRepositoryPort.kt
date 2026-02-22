package com.verobee.vergate.ports.out

import com.verobee.vergate.domain.model.AppVersion

interface AppVersionRepositoryPort {
    fun findById(id: String): AppVersion?
    fun findByAppId(appId: String): List<AppVersion>
    fun findActiveByAppId(appId: String): AppVersion?
    fun save(version: AppVersion): AppVersion
    fun deleteById(id: String)
}
