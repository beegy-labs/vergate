package com.verobee.vergate.unit

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.domain.model.RemoteConfig
import com.verobee.vergate.domain.model.ValueType
import com.verobee.vergate.domain.service.RemoteConfigService
import com.verobee.vergate.ports.out.GatewayCachePort
import com.verobee.vergate.ports.out.RemoteConfigRepositoryPort
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RemoteConfigServiceTest {

    private val configRepository = mockk<RemoteConfigRepositoryPort>()
    private val cache = mockk<GatewayCachePort>(relaxed = true)
    private val service = RemoteConfigService(configRepository, cache)

    private val APP_ID = "019500a0-0000-7000-8000-000000000100"
    private val CFG_ID = "019500a0-0000-7000-8000-000000000001"

    private fun config(id: String = CFG_ID, appId: String = APP_ID) = RemoteConfig(
        id = id, appId = appId, configKey = "feature_x",
        configValue = "true", valueType = ValueType.BOOLEAN,
        description = "Feature X toggle", abRatio = 100,
    )

    @Test
    fun `create saves config and evicts cache`() {
        every { configRepository.existsByAppIdAndConfigKey(APP_ID, "feature_x") } returns false
        every { configRepository.save(any()) } returns config()

        val result = service.create(APP_ID, "feature_x", "true", "BOOLEAN", "Feature X toggle", 100)

        assertEquals("feature_x", result.configKey)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `create throws on duplicate key`() {
        every { configRepository.existsByAppIdAndConfigKey(APP_ID, "feature_x") } returns true

        assertThrows(ApiException::class.java) {
            service.create(APP_ID, "feature_x", "true", "BOOLEAN", null, 100)
        }
    }

    @Test
    fun `update modifies config and evicts cache`() {
        every { configRepository.findById(CFG_ID) } returns config()
        every { configRepository.save(any()) } answers { firstArg() }

        val result = service.update(CFG_ID, "false", "BOOLEAN", "Disabled", 50, false)

        assertEquals("false", result.configValue)
        assertEquals(50, result.abRatio)
        assertFalse(result.isActive)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `delete removes config and evicts cache`() {
        every { configRepository.findById(CFG_ID) } returns config()
        every { configRepository.deleteById(CFG_ID) } just runs

        service.delete(CFG_ID)

        verify { configRepository.deleteById(CFG_ID) }
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `findById throws when not found`() {
        every { configRepository.findById("non-existent") } returns null

        assertThrows(ApiException::class.java) {
            service.findById("non-existent")
        }
    }

    @Test
    fun `findByAppId returns list`() {
        every { configRepository.findByAppId(APP_ID) } returns listOf(config())

        val result = service.findByAppId(APP_ID)

        assertEquals(1, result.size)
    }
}
