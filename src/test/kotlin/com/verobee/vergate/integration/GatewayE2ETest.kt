package com.verobee.vergate.integration

import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateAppRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateMaintenanceRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateVersionRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateAppRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayE2ETest : TestcontainersConfig() {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private fun createApp(name: String = "E2E App", platform: String = "ANDROID", storeUrl: String? = null): Pair<String, String> {
        val res = restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps",
            CreateAppRequest(name = name, platform = platform, storeUrl = storeUrl),
        )
        assertEquals(HttpStatus.CREATED, res.statusCode)
        @Suppress("UNCHECKED_CAST")
        val data = res.body!!["data"] as Map<String, Any>
        val appId = data["id"] as String
        val appKey = data["appKey"] as String
        return appId to appKey
    }

    @Test
    fun `E2E happy path - create app, set version, call init`() {
        val (appId, appKey) = createApp(
            storeUrl = "https://play.google.com/store/apps/details?id=e2e",
        )

        // 2. Create version rule
        val createVersion = restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/versions",
            CreateVersionRequest(minVersion = "2.0.0", latestVersion = "3.0.0"),
        )
        assertEquals(HttpStatus.CREATED, createVersion.statusCode)

        // 3. Call init
        @Suppress("UNCHECKED_CAST")
        val init = restTemplate.getForEntity(
            "/api/v1/init?app_key=$appKey&platform=ANDROID&app_version=2.5.0",
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, init.statusCode)

        val data = init.body!!["data"] as Map<String, Any>
        val service = data["service"] as Map<String, Any>
        val update = data["update"] as Map<String, Any>

        assertTrue(service["active"] as Boolean)
        assertFalse(update["force"] as Boolean)
        assertEquals("3.0.0", update["latest_version"])
        assertEquals("2.0.0", update["min_version"])
    }

    @Test
    fun `E2E service termination - deactivated app returns blocked`() {
        val (appId, appKey) = createApp(name = "Terminated App", platform = "IOS")

        // 2. Deactivate app
        restTemplate.exchange(
            "/api/v1/admin/apps/$appId",
            HttpMethod.PUT,
            HttpEntity(UpdateAppRequest(name = "Terminated App", platform = "IOS", isActive = false)),
            Map::class.java,
        )

        // 3. Call init — should be blocked
        @Suppress("UNCHECKED_CAST")
        val init = restTemplate.getForEntity(
            "/api/v1/init?app_key=$appKey&platform=IOS&app_version=1.0.0",
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, init.statusCode)

        val data = init.body!!["data"] as Map<String, Any>
        val service = data["service"] as Map<String, Any>

        assertFalse(service["active"] as Boolean)
        assertNotNull(service["message"])
        assertNull(data["update"])
    }

    @Test
    fun `E2E force update - old version gets force=true`() {
        val (appId, appKey) = createApp(name = "Update App")

        // 2. Set version rule: min 2.0.0
        restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/versions",
            CreateVersionRequest(minVersion = "2.0.0", latestVersion = "3.0.0"),
        )

        // 3. Call init with old version 1.0.0
        @Suppress("UNCHECKED_CAST")
        val init = restTemplate.getForEntity(
            "/api/v1/init?app_key=$appKey&platform=ANDROID&app_version=1.0.0",
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, init.statusCode)

        val data = init.body!!["data"] as Map<String, Any>
        val update = data["update"] as Map<String, Any>

        assertTrue(update["force"] as Boolean)
        assertEquals("2.0.0", update["min_version"])
    }
}
