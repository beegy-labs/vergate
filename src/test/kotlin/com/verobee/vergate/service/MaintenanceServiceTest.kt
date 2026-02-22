package com.verobee.vergate.service

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.domain.model.Maintenance
import com.verobee.vergate.domain.service.MaintenanceService
import com.verobee.vergate.ports.out.GatewayCachePort
import com.verobee.vergate.ports.out.MaintenanceRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime

class MaintenanceServiceTest {

    private val maintenanceRepository: MaintenanceRepositoryPort = mockk(relaxed = true)
    private val cache: GatewayCachePort = mockk(relaxed = true)
    private lateinit var service: MaintenanceService

    private val now = OffsetDateTime.now()
    private val APP_ID = "019500a0-0000-7000-8000-000000000100"
    private val MNT_ID = "019500a0-0000-7000-8000-000000000001"

    private val testMaintenance = Maintenance(
        id = MNT_ID, appId = APP_ID, title = "Server Update",
        message = "Updating servers", startAt = now, endAt = now.plusHours(2),
    )

    @BeforeEach
    fun setUp() {
        service = MaintenanceService(maintenanceRepository, cache)
    }

    @Test
    fun `create saves maintenance and evicts cache`() {
        every { maintenanceRepository.save(any()) } returns testMaintenance

        val result = service.create(APP_ID, "Server Update", "Updating servers", now, now.plusHours(2))

        assertEquals("Server Update", result.title)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `update modifies maintenance and evicts cache`() {
        every { maintenanceRepository.findById(MNT_ID) } returns testMaintenance
        every { maintenanceRepository.save(any()) } answers { firstArg() }

        val newEnd = now.plusHours(4)
        val result = service.update(MNT_ID, "Extended Maintenance", "Taking longer", now, newEnd, true)

        assertEquals("Extended Maintenance", result.title)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `update can deactivate maintenance`() {
        every { maintenanceRepository.findById(MNT_ID) } returns testMaintenance
        every { maintenanceRepository.save(any()) } answers { firstArg() }

        val result = service.update(MNT_ID, "Server Update", null, now, now.plusHours(2), false)

        assertFalse(result.isActive)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `delete removes maintenance and evicts cache`() {
        every { maintenanceRepository.findById(MNT_ID) } returns testMaintenance

        service.delete(MNT_ID)

        verify { maintenanceRepository.deleteById(MNT_ID) }
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `findById throws when not found`() {
        every { maintenanceRepository.findById("non-existent") } returns null

        assertThrows<ApiException> {
            service.findById("non-existent")
        }
    }

    @Test
    fun `findByAppId returns all maintenances for app`() {
        val MNT_ID2 = "019500a0-0000-7000-8000-000000000002"
        every { maintenanceRepository.findByAppId(APP_ID) } returns listOf(
            testMaintenance,
            testMaintenance.copy(id = MNT_ID2, title = "Another")
        )

        val result = service.findByAppId(APP_ID)

        assertEquals(2, result.size)
    }
}
