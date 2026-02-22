package com.verobee.vergate.service

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.domain.model.App
import com.verobee.vergate.domain.model.Platform
import com.verobee.vergate.domain.service.AppService
import com.verobee.vergate.ports.out.AppRepositoryPort
import com.verobee.vergate.ports.out.GatewayCachePort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AppServiceTest {

    private val appRepository: AppRepositoryPort = mockk(relaxed = true)
    private val cache: GatewayCachePort = mockk(relaxed = true)
    private lateinit var service: AppService

    private val APP_ID = "019500a0-0000-7000-8000-000000000001"

    private val testApp = App(
        id = APP_ID, appKey = "0FzKQ8nVwLpJ2mXeRgBs3c", name = "My App",
        platform = Platform.ANDROID, storeUrl = "https://play.google.com",
    )

    @BeforeEach
    fun setUp() {
        service = AppService(appRepository, cache)
    }

    @Test
    fun `create saves app successfully`() {
        every { appRepository.save(any()) } answers { firstArg() }

        val result = service.create("New App", null, "ANDROID", null)

        assertEquals("New App", result.name)
        assertEquals(Platform.ANDROID, result.platform)
        verify { appRepository.save(any()) }
    }

    @Test
    fun `update modifies app and evicts cache`() {
        every { appRepository.findById(APP_ID) } returns testApp
        every { appRepository.save(any()) } answers { firstArg() }

        val result = service.update(APP_ID, "Updated Name", "desc", "IOS", null, true)

        assertEquals("Updated Name", result.name)
        assertEquals(Platform.IOS, result.platform)
        verify { cache.evictByAppKey(testApp.appKey) }
    }

    @Test
    fun `update with isActive=false triggers cache eviction`() {
        every { appRepository.findById(APP_ID) } returns testApp
        every { appRepository.save(any()) } answers { firstArg() }

        val result = service.update(APP_ID, "My App", null, "ANDROID", null, false)

        assertFalse(result.isActive)
        verify { cache.evictByAppKey(testApp.appKey) }
    }

    @Test
    fun `delete removes app and evicts cache`() {
        every { appRepository.findById(APP_ID) } returns testApp

        service.delete(APP_ID)

        verify { cache.evictByAppKey(testApp.appKey) }
        verify { appRepository.deleteById(APP_ID) }
    }

    @Test
    fun `findById throws when not found`() {
        every { appRepository.findById("non-existent-id") } returns null

        assertThrows<ApiException> {
            service.findById("non-existent-id")
        }
    }

    @Test
    fun `findAll returns all apps`() {
        val OTHER_ID = "019500a0-0000-7000-8000-000000000002"
        every { appRepository.findAll() } returns listOf(testApp, testApp.copy(id = OTHER_ID, appKey = "other"))

        val result = service.findAll()

        assertEquals(2, result.size)
    }
}
