package com.verobee.vergate.ports.out

import com.verobee.vergate.domain.model.RemoteConfig

interface RemoteConfigRepositoryPort {
    fun findById(id: String): RemoteConfig?
    fun findByAppId(appId: String): List<RemoteConfig>
    fun findActiveByAppId(appId: String): List<RemoteConfig>
    fun save(config: RemoteConfig): RemoteConfig
    fun deleteById(id: String)
    fun existsByAppIdAndConfigKey(appId: String, configKey: String): Boolean
}
