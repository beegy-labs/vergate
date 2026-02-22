package com.verobee.vergate.controller

import com.ninjasquad.springmockk.MockkBean
import com.verobee.vergate.adapters.`in`.rest.client.GatewayController
import com.verobee.vergate.adapters.`in`.rest.dto.client.*
import com.verobee.vergate.domain.service.GatewayService
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(GatewayController::class)
@Import(com.verobee.vergate.config.SecurityConfig::class)
class GatewayControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var gatewayService: GatewayService

    @Test
    fun `init returns 200 with active service`() {
        every { gatewayService.init("test-app", "ANDROID", "1.0.0") } returns InitResponse(
            service = ServiceStatus(active = true),
            update = UpdateInfo(force = false, latestVersion = "2.0.0", minVersion = "1.0.0", storeUrl = null),
            maintenance = MaintenanceInfo(active = false),
            notices = emptyList(),
            config = mapOf("feature_x" to true),
            legal = emptyList(),
        )

        mockMvc.get("/api/v1/init") {
            param("app_key", "test-app")
            param("platform", "ANDROID")
            param("app_version", "1.0.0")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.service.active") { value(true) }
            jsonPath("$.data.update.latest_version") { value("2.0.0") }
            jsonPath("$.data.config.feature_x") { value(true) }
        }
    }

    @Test
    fun `init returns terminated service when inactive`() {
        every { gatewayService.init("dead-app", "IOS", "1.0.0") } returns InitResponse(
            service = ServiceStatus(active = false, message = "This service has been terminated."),
            update = null,
            maintenance = null,
            notices = emptyList(),
            config = emptyMap(),
            legal = emptyList(),
        )

        mockMvc.get("/api/v1/init") {
            param("app_key", "dead-app")
            param("platform", "IOS")
            param("app_version", "1.0.0")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.service.active") { value(false) }
            jsonPath("$.data.service.message") { value("This service has been terminated.") }
            jsonPath("$.data.update") { doesNotExist() }
        }
    }
}
