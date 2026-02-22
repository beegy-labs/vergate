package com.verobee.vergate.service

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.domain.model.AppVersion
import com.verobee.vergate.domain.service.VersionService
import com.verobee.vergate.ports.out.AppVersionRepositoryPort
import com.verobee.vergate.ports.out.GatewayCachePort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VersionServiceTest {

    private val versionRepository: AppVersionRepositoryPort = mockk(relaxed = true)
    private val cache: GatewayCachePort = mockk(relaxed = true)
    private lateinit var service: VersionService

    private val APP_ID = "019500a0-0000-7000-8000-000000000100"
    private val VER_ID = "019500a0-0000-7000-8000-000000000001"

    @BeforeEach
    fun setUp() {
        service = VersionService(versionRepository, cache)
    }

    @Test
    fun `create saves version and evicts cache`() {
        val expected = AppVersion(id = VER_ID, appId = APP_ID, minVersion = "1.0.0", latestVersion = "2.0.0")
        every { versionRepository.save(any()) } returns expected

        val result = service.create(APP_ID, "1.0.0", "2.0.0", false, null)

        assertEquals("2.0.0", result.latestVersion)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `update modifies version and evicts cache`() {
        val existing = AppVersion(id = VER_ID, appId = APP_ID, minVersion = "1.0.0", latestVersion = "2.0.0")
        every { versionRepository.findById(VER_ID) } returns existing
        every { versionRepository.save(any()) } answers { firstArg() }

        val result = service.update(VER_ID, "1.5.0", "3.0.0", true, "Please update", true)

        assertEquals("3.0.0", result.latestVersion)
        assertEquals("1.5.0", result.minVersion)
        assertTrue(result.forceUpdate)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `findById throws when not found`() {
        every { versionRepository.findById("non-existent") } returns null

        assertThrows<ApiException> {
            service.findById("non-existent")
        }
    }
}
