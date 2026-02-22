package com.verobee.vergate.adapters.out.persistence.adapter

import com.verobee.vergate.adapters.out.persistence.mapper.NoticeMapper
import com.verobee.vergate.adapters.out.persistence.repository.NoticeJpaRepository
import com.verobee.vergate.domain.model.Notice
import com.verobee.vergate.ports.out.NoticeRepositoryPort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class NoticeJpaAdapter(
    private val jpaRepository: NoticeJpaRepository,
    private val mapper: NoticeMapper,
) : NoticeRepositoryPort {

    override fun findById(id: String): Notice? =
        jpaRepository.findByIdOrNull(UUID.fromString(id))?.let(mapper::toDomain)

    override fun findByAppId(appId: String): List<Notice> =
        jpaRepository.findByAppId(UUID.fromString(appId)).map(mapper::toDomain)

    override fun findActiveByAppIdAndTime(appId: String, now: OffsetDateTime): List<Notice> =
        jpaRepository.findActiveByAppIdAndTime(UUID.fromString(appId), now).map(mapper::toDomain)

    override fun save(notice: Notice): Notice {
        val entity = if (notice.id.isEmpty()) {
            mapper.toEntity(notice)
        } else {
            jpaRepository.findByIdOrNull(UUID.fromString(notice.id))?.also { mapper.updateEntity(it, notice) }
                ?: mapper.toEntity(notice)
        }
        return mapper.toDomain(jpaRepository.save(entity))
    }

    override fun deleteById(id: String) = jpaRepository.deleteById(UUID.fromString(id))
}
