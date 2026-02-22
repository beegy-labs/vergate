package com.verobee.vergate.integration

import com.verobee.vergate.adapters.out.persistence.entity.AppJpaEntity
import com.verobee.vergate.adapters.out.persistence.entity.MaintenanceJpaEntity
import com.verobee.vergate.adapters.out.persistence.entity.PlatformJpa
import com.verobee.vergate.adapters.out.persistence.entity.RemoteConfigJpaEntity
import com.verobee.vergate.adapters.out.persistence.entity.ValueTypeJpa
import com.verobee.vergate.adapters.out.persistence.repository.AppJpaRepository
import com.verobee.vergate.adapters.out.persistence.repository.MaintenanceJpaRepository
import com.verobee.vergate.adapters.out.persistence.repository.RemoteConfigJpaRepository
import com.verobee.vergate.domain.util.AppKeyGenerator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
class JpaAdapterIntegrationTest : TestcontainersConfig() {

    @Autowired
    private lateinit var appRepository: AppJpaRepository

    @Autowired
    private lateinit var maintenanceRepository: MaintenanceJpaRepository

    @Autowired
    private lateinit var configRepository: RemoteConfigJpaRepository

    private lateinit var testAppId: UUID

    @BeforeEach
    fun setUp() {
        configRepository.deleteAll()
        maintenanceRepository.deleteAll()
        appRepository.deleteAll()

        val (uuid, appKey) = AppKeyGenerator.generate()
        val app = appRepository.save(
            AppJpaEntity(
                id = uuid,
                appKey = appKey,
                name = "Integration Test App",
                platform = PlatformJpa.ANDROID,
            )
        )
        testAppId = app.id
    }

    @Test
    fun `maintenance time-range query returns only active within window`() {
        val now = OffsetDateTime.now()

        // Active maintenance (now is within window)
        maintenanceRepository.save(
            MaintenanceJpaEntity(
                id = AppKeyGenerator.generateUuidV7(),
                appId = testAppId, title = "Current",
                startAt = now.minusHours(1), endAt = now.plusHours(1),
            )
        )
        // Past maintenance (ended)
        maintenanceRepository.save(
            MaintenanceJpaEntity(
                id = AppKeyGenerator.generateUuidV7(),
                appId = testAppId, title = "Past",
                startAt = now.minusHours(3), endAt = now.minusHours(1),
            )
        )
        // Future maintenance (not started)
        maintenanceRepository.save(
            MaintenanceJpaEntity(
                id = AppKeyGenerator.generateUuidV7(),
                appId = testAppId, title = "Future",
                startAt = now.plusHours(1), endAt = now.plusHours(3),
            )
        )
        // Inactive maintenance (within window but disabled)
        maintenanceRepository.save(
            MaintenanceJpaEntity(
                id = AppKeyGenerator.generateUuidV7(),
                appId = testAppId, title = "Disabled",
                startAt = now.minusHours(1), endAt = now.plusHours(1),
                isActive = false,
            )
        )

        val results = maintenanceRepository.findActiveByAppIdAndTime(testAppId, now)

        assertEquals(1, results.size)
        assertEquals("Current", results[0].title)
    }

    @Test
    fun `remote config unique constraint on app_id + config_key`() {
        configRepository.save(
            RemoteConfigJpaEntity(
                id = AppKeyGenerator.generateUuidV7(),
                appId = testAppId, configKey = "feature_x",
                configValue = "true", valueType = ValueTypeJpa.BOOLEAN,
            )
        )

        assertThrows(DataIntegrityViolationException::class.java) {
            configRepository.save(
                RemoteConfigJpaEntity(
                    id = AppKeyGenerator.generateUuidV7(),
                    appId = testAppId, configKey = "feature_x",
                    configValue = "false", valueType = ValueTypeJpa.BOOLEAN,
                )
            )
            configRepository.flush()
        }
    }

    @Test
    fun `app cascade delete removes child records`() {
        maintenanceRepository.save(
            MaintenanceJpaEntity(
                id = AppKeyGenerator.generateUuidV7(),
                appId = testAppId, title = "To be cascaded",
                startAt = OffsetDateTime.now(), endAt = OffsetDateTime.now().plusHours(1),
            )
        )
        configRepository.save(
            RemoteConfigJpaEntity(
                id = AppKeyGenerator.generateUuidV7(),
                appId = testAppId, configKey = "cascade_test",
                configValue = "value", valueType = ValueTypeJpa.STRING,
            )
        )

        appRepository.deleteById(testAppId)
        appRepository.flush()

        assertTrue(maintenanceRepository.findByAppId(testAppId).isEmpty())
        assertTrue(configRepository.findByAppId(testAppId).isEmpty())
    }
}
