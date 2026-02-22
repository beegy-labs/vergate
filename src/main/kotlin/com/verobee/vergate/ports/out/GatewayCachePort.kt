package com.verobee.vergate.ports.out

interface GatewayCachePort {
    fun getInitResponse(cacheKey: String): String?
    fun setInitResponse(cacheKey: String, json: String, ttlSeconds: Long)
    fun evictByAppKey(appKey: String)
    fun evictByAppId(appId: String)
}
