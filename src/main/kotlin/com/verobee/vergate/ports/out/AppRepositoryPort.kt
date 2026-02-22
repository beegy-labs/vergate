package com.verobee.vergate.ports.out

import com.verobee.vergate.domain.model.App

interface AppRepositoryPort {
    fun findById(id: String): App?
    fun findByAppKey(appKey: String): App?
    fun findAll(): List<App>
    fun save(app: App): App
    fun deleteById(id: String)
    fun existsByAppKey(appKey: String): Boolean
}
