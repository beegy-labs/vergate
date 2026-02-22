package com.verobee.vergate.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.domain.model.*
import com.verobee.vergate.domain.service.GatewayService
import com.verobee.vergate.ports.out.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime

class GatewayServiceTest {

    private val appRepository: AppRepositoryPort = mockk()
    private val versionRepository: AppVersionRepositoryPort = mockk()
    private val maintenanceRepository: MaintenanceRepositoryPort = mockk()
    private val noticeRepository: NoticeRepositoryPort = mockk()
    private val configRepository: RemoteConfigRepositoryPort = mockk()
    private val legalDocRepository: LegalDocumentRepositoryPort = mockk(relaxed = true)
    private val cache: GatewayCachePort = mockk(relaxed = true)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private lateinit var service: GatewayService

    private val APP_ID = "019500a0-0000-7000-8000-000000000001"
    private val VER_ID = "019500a0-0000-7000-8000-000000000002"
    private val MNT_ID = "019500a0-0000-7000-8000-000000000003"
    private val CFG_ID1 = "019500a0-0000-7000-8000-000000000004"
    private val CFG_ID2 = "019500a0-0000-7000-8000-000000000005"
    private val CFG_ID3 = "019500a0-0000-7000-8000-000000000006"

    private val testApp = App(
        id = APP_ID, appKey = "test-app", name = "Test App",
        platform = Platform.ANDROID, storeUrl = "https://play.google.com/store/apps/details?id=test",
        isActive = true,
    )

    @BeforeEach
    fun setUp() {
        service = GatewayService(
            appRepository, versionRepository, maintenanceRepository,
            noticeRepository, configRepository, legalDocRepository, cache, objectMapper, 60L,
        )
        every { legalDocRepository.findActiveByAppId(any()) } returns emptyList()
    }

    @Test
    fun `init returns service terminated when app is inactive`() {
        every { cache.getInitResponse(any()) } returns null
        every { appRepository.findByAppKey("dead-app") } returns testApp.copy(isActive = false)

        val result = service.init("dead-app", "ANDROID", "1.0.0")

        assertFalse(result.service.active)
        assertNull(result.update)
        assertTrue(result.notices.isEmpty())
    }

    @Test
    fun `init throws when app not found`() {
        every { cache.getInitResponse(any()) } returns null
        every { appRepository.findByAppKey("unknown") } returns null

        assertThrows<ApiException> {
            service.init("unknown", "ANDROID", "1.0.0")
        }
    }

    @Test
    fun `init returns force update when app version is below min`() {
        every { cache.getInitResponse(any()) } returns null
        every { appRepository.findByAppKey("test-app") } returns testApp
        every { versionRepository.findActiveByAppId(APP_ID) } returns AppVersion(
            id = VER_ID, appId = APP_ID, minVersion = "2.0.0", latestVersion = "2.5.0", forceUpdate = false,
        )
        every { maintenanceRepository.findActiveByAppIdAndTime(eq(APP_ID), any()) } returns null
        every { noticeRepository.findActiveByAppIdAndTime(eq(APP_ID), any()) } returns emptyList()
        every { configRepository.findActiveByAppId(APP_ID) } returns emptyList()

        val result = service.init("test-app", "ANDROID", "1.5.0")

        assertTrue(result.service.active)
        assertNotNull(result.update)
        assertTrue(result.update!!.force)
        assertEquals("2.5.0", result.update!!.latestVersion)
        assertEquals("2.0.0", result.update!!.minVersion)
    }

    @Test
    fun `init returns non-force update when above min but below latest`() {
        every { cache.getInitResponse(any()) } returns null
        every { appRepository.findByAppKey("test-app") } returns testApp
        every { versionRepository.findActiveByAppId(APP_ID) } returns AppVersion(
            id = VER_ID, appId = APP_ID, minVersion = "2.0.0", latestVersion = "2.5.0", forceUpdate = false,
        )
        every { maintenanceRepository.findActiveByAppIdAndTime(eq(APP_ID), any()) } returns null
        every { noticeRepository.findActiveByAppIdAndTime(eq(APP_ID), any()) } returns emptyList()
        every { configRepository.findActiveByAppId(APP_ID) } returns emptyList()

        val result = service.init("test-app", "ANDROID", "2.1.0")

        assertTrue(result.service.active)
        assertNotNull(result.update)
        assertFalse(result.update!!.force)
    }

    @Test
    fun `init returns cached response when available`() {
        val cachedJson = objectMapper.writeValueAsString(
            com.verobee.vergate.adapters.`in`.rest.dto.client.InitResponse(
                service = com.verobee.vergate.adapters.`in`.rest.dto.client.ServiceStatus(active = true),
                update = null, maintenance = null, notices = emptyList(), config = emptyMap(),
                legal = emptyList(),
            )
        )
        every { cache.getInitResponse("test-app:ANDROID:1.0.0") } returns cachedJson

        val result = service.init("test-app", "ANDROID", "1.0.0")

        assertTrue(result.service.active)
        verify(exactly = 0) { appRepository.findByAppKey(any()) }
    }

    @Test
    fun `init includes active maintenance`() {
        val now = OffsetDateTime.now()
        every { cache.getInitResponse(any()) } returns null
        every { appRepository.findByAppKey("test-app") } returns testApp
        every { versionRepository.findActiveByAppId(APP_ID) } returns null
        every { maintenanceRepository.findActiveByAppIdAndTime(eq(APP_ID), any()) } returns Maintenance(
            id = MNT_ID, appId = APP_ID, title = "Server Maintenance",
            message = "Updating servers", startAt = now.minusHours(1), endAt = now.plusHours(1),
        )
        every { noticeRepository.findActiveByAppIdAndTime(eq(APP_ID), any()) } returns emptyList()
        every { configRepository.findActiveByAppId(APP_ID) } returns emptyList()

        val result = service.init("test-app", "ANDROID", "1.0.0")

        assertNotNull(result.maintenance)
        assertTrue(result.maintenance!!.active)
        assertEquals("Server Maintenance", result.maintenance!!.title)
    }

    @Test
    fun `init includes remote config with parsed types`() {
        every { cache.getInitResponse(any()) } returns null
        every { appRepository.findByAppKey("test-app") } returns testApp
        every { versionRepository.findActiveByAppId(APP_ID) } returns null
        every { maintenanceRepository.findActiveByAppIdAndTime(eq(APP_ID), any()) } returns null
        every { noticeRepository.findActiveByAppIdAndTime(eq(APP_ID), any()) } returns emptyList()
        every { configRepository.findActiveByAppId(APP_ID) } returns listOf(
            RemoteConfig(id = CFG_ID1, appId = APP_ID, configKey = "feature_x", configValue = "true", valueType = ValueType.BOOLEAN),
            RemoteConfig(id = CFG_ID2, appId = APP_ID, configKey = "max_retry", configValue = "3", valueType = ValueType.NUMBER),
            RemoteConfig(id = CFG_ID3, appId = APP_ID, configKey = "theme", configValue = "dark", valueType = ValueType.STRING),
        )

        val result = service.init("test-app", "ANDROID", "1.0.0")

        assertEquals(true, result.config["feature_x"])
        assertEquals(3L, result.config["max_retry"])
        assertEquals("dark", result.config["theme"])
    }
}
