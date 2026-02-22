package com.verobee.vergate.adapters.out.persistence.adapter

import com.verobee.vergate.adapters.out.persistence.mapper.AppMapper
import com.verobee.vergate.adapters.out.persistence.repository.AppJpaRepository
import com.verobee.vergate.domain.model.App
import com.verobee.vergate.ports.out.AppRepositoryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AppJpaAdapter(
    private val jpaRepository: AppJpaRepository,
    private val mapper: AppMapper,
) : AppRepositoryPort {

    override fun findById(id: String): App? =
        jpaRepository.findByIdOrNull(UUID.fromString(id))?.let(mapper::toDomain)

    override fun findByAppKey(appKey: String): App? =
        jpaRepository.findByAppKey(appKey)?.let(mapper::toDomain)

    override fun findAll(): List<App> =
        jpaRepository.findAll().map(mapper::toDomain)

    override fun save(app: App): App {
        val entity = if (app.id.isEmpty()) {
            mapper.toEntity(app)
        } else {
            jpaRepository.findByIdOrNull(UUID.fromString(app.id))?.also { mapper.updateEntity(it, app) }
                ?: mapper.toEntity(app)
        }
        return mapper.toDomain(jpaRepository.save(entity))
    }

    override fun deleteById(id: String) = jpaRepository.deleteById(UUID.fromString(id))

    override fun existsByAppKey(appKey: String): Boolean =
        jpaRepository.existsByAppKey(appKey)
}
