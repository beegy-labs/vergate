package com.verobee.vergate.integration

import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateAppRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateConfigRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateMaintenanceRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateNoticeRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateVersionRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateAppRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateConfigRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateMaintenanceRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateNoticeRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateVersionRequest
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
class AdminCrudE2ETest : TestcontainersConfig() {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private fun createApp(): Pair<String, String> {
        val res = restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps",
            CreateAppRequest(name = "CRUD Test App", platform = "ANDROID"),
        )
        assertEquals(HttpStatus.CREATED, res.statusCode)
        @Suppress("UNCHECKED_CAST")
        val data = res.body!!["data"] as Map<String, Any>
        val appId = data["id"] as String
        val appKey = data["appKey"] as String
        return appId to appKey
    }

    // ─── App CRUD ────────────────────────────────────────────

    @Test
    fun `list apps returns all apps`() {
        createApp()
        val res = restTemplate.getForEntity("/api/v1/admin/apps", Map::class.java)
        assertEquals(HttpStatus.OK, res.statusCode)
        assertTrue(res.body!!["success"] as Boolean)
        @Suppress("UNCHECKED_CAST")
        val data = res.body!!["data"] as List<*>
        assertTrue(data.isNotEmpty())
    }

    @Test
    fun `get app by id returns app details`() {
        val (appId, _) = createApp()
        val res = restTemplate.getForEntity("/api/v1/admin/apps/$appId", Map::class.java)
        assertEquals(HttpStatus.OK, res.statusCode)
        @Suppress("UNCHECKED_CAST")
        val data = res.body!!["data"] as Map<String, Any>
        assertEquals("CRUD Test App", data["name"])
        assertNotNull(data["appKey"])
    }

    @Test
    fun `update app changes fields`() {
        val (appId, _) = createApp()
        val res = restTemplate.exchange(
            "/api/v1/admin/apps/$appId", HttpMethod.PUT,
            HttpEntity(UpdateAppRequest(name = "Updated App", platform = "IOS", storeUrl = "https://store.example.com")),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, res.statusCode)
        @Suppress("UNCHECKED_CAST")
        val data = res.body!!["data"] as Map<String, Any>
        assertEquals("Updated App", data["name"])
        assertEquals("IOS", data["platform"])
    }

    @Test
    fun `delete app returns 204`() {
        val (appId, _) = createApp()
        val res = restTemplate.exchange(
            "/api/v1/admin/apps/$appId", HttpMethod.DELETE, null, Void::class.java,
        )
        assertEquals(HttpStatus.NO_CONTENT, res.statusCode)
    }

    // ─── Version CRUD ────────────────────────────────────────

    @Test
    fun `version CRUD - create, list, update`() {
        val (appId, _) = createApp()

        // Create
        val create = restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/versions",
            CreateVersionRequest(minVersion = "1.0.0", latestVersion = "2.0.0", updateMessage = "Please update"),
        )
        assertEquals(HttpStatus.CREATED, create.statusCode)
        @Suppress("UNCHECKED_CAST")
        val created = create.body!!["data"] as Map<String, Any>
        val versionId = created["id"] as String
        assertEquals("1.0.0", created["minVersion"])

        // List
        val list = restTemplate.getForEntity("/api/v1/admin/apps/$appId/versions", Map::class.java)
        assertEquals(HttpStatus.OK, list.statusCode)
        @Suppress("UNCHECKED_CAST")
        val versions = list.body!!["data"] as List<*>
        assertTrue(versions.isNotEmpty())

        // Update
        val update = restTemplate.exchange(
            "/api/v1/admin/apps/$appId/versions/$versionId", HttpMethod.PUT,
            HttpEntity(UpdateVersionRequest(minVersion = "1.5.0", latestVersion = "3.0.0", forceUpdate = true)),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, update.statusCode)
        @Suppress("UNCHECKED_CAST")
        val updated = update.body!!["data"] as Map<String, Any>
        assertEquals("1.5.0", updated["minVersion"])
        assertEquals("3.0.0", updated["latestVersion"])
        assertEquals(true, updated["forceUpdate"])
    }

    // ─── Maintenance CRUD ────────────────────────────────────

    @Test
    fun `maintenance CRUD - create, list, update, delete`() {
        val (appId, _) = createApp()
        val now = OffsetDateTime.now()

        // Create
        val create = restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/maintenances",
            CreateMaintenanceRequest(
                title = "Scheduled Maintenance", message = "We are upgrading servers",
                startAt = now.plusHours(1), endAt = now.plusHours(3),
            ),
        )
        assertEquals(HttpStatus.CREATED, create.statusCode)
        @Suppress("UNCHECKED_CAST")
        val created = create.body!!["data"] as Map<String, Any>
        val maintenanceId = created["id"] as String
        assertEquals("Scheduled Maintenance", created["title"])

        // List
        val list = restTemplate.getForEntity("/api/v1/admin/apps/$appId/maintenances", Map::class.java)
        assertEquals(HttpStatus.OK, list.statusCode)
        @Suppress("UNCHECKED_CAST")
        val maintenances = list.body!!["data"] as List<*>
        assertEquals(1, maintenances.size)

        // Update
        val update = restTemplate.exchange(
            "/api/v1/admin/apps/$appId/maintenances/$maintenanceId", HttpMethod.PUT,
            HttpEntity(
                UpdateMaintenanceRequest(
                    title = "Updated Maintenance", message = "Rescheduled",
                    startAt = now.plusHours(2), endAt = now.plusHours(4), isActive = false,
                )
            ),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, update.statusCode)
        @Suppress("UNCHECKED_CAST")
        val updated = update.body!!["data"] as Map<String, Any>
        assertEquals("Updated Maintenance", updated["title"])
        assertEquals(false, updated["isActive"])

        // Delete
        val delete = restTemplate.exchange(
            "/api/v1/admin/apps/$appId/maintenances/$maintenanceId", HttpMethod.DELETE,
            null, Void::class.java,
        )
        assertEquals(HttpStatus.NO_CONTENT, delete.statusCode)
    }

    // ─── Notice CRUD ─────────────────────────────────────────

    @Test
    fun `notice CRUD - create, list, update, delete`() {
        val (appId, _) = createApp()
        val now = OffsetDateTime.now()

        // Create
        val create = restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/notices",
            CreateNoticeRequest(
                title = "New Event", message = "Join us!", imageUrl = "https://img.example.com/event.png",
                deepLink = "app://event/1", displayType = "DAILY", priority = 10,
                startAt = now, endAt = now.plusDays(7),
            ),
        )
        assertEquals(HttpStatus.CREATED, create.statusCode)
        @Suppress("UNCHECKED_CAST")
        val created = create.body!!["data"] as Map<String, Any>
        val noticeId = created["id"] as String
        assertEquals("New Event", created["title"])
        assertEquals("DAILY", created["displayType"])
        assertEquals(10, (created["priority"] as Number).toInt())

        // List
        val list = restTemplate.getForEntity("/api/v1/admin/apps/$appId/notices", Map::class.java)
        assertEquals(HttpStatus.OK, list.statusCode)
        @Suppress("UNCHECKED_CAST")
        val notices = list.body!!["data"] as List<*>
        assertEquals(1, notices.size)

        // Update
        val update = restTemplate.exchange(
            "/api/v1/admin/apps/$appId/notices/$noticeId", HttpMethod.PUT,
            HttpEntity(
                UpdateNoticeRequest(
                    title = "Updated Event", message = "Updated!", displayType = "ALWAYS",
                    priority = 20, isActive = false,
                )
            ),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, update.statusCode)
        @Suppress("UNCHECKED_CAST")
        val updated = update.body!!["data"] as Map<String, Any>
        assertEquals("Updated Event", updated["title"])
        assertEquals("ALWAYS", updated["displayType"])
        assertEquals(false, updated["isActive"])

        // Delete
        val delete = restTemplate.exchange(
            "/api/v1/admin/apps/$appId/notices/$noticeId", HttpMethod.DELETE,
            null, Void::class.java,
        )
        assertEquals(HttpStatus.NO_CONTENT, delete.statusCode)
    }

    // ─── Config CRUD ─────────────────────────────────────────

    @Test
    fun `config CRUD - create, list, update, delete`() {
        val (appId, _) = createApp()

        // Create
        val create = restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/configs",
            CreateConfigRequest(
                configKey = "feature_dark_mode", configValue = "true",
                valueType = "BOOLEAN", description = "Dark mode toggle", abRatio = 50,
            ),
        )
        assertEquals(HttpStatus.CREATED, create.statusCode)
        @Suppress("UNCHECKED_CAST")
        val created = create.body!!["data"] as Map<String, Any>
        val configId = created["id"] as String
        assertEquals("feature_dark_mode", created["configKey"])
        assertEquals("BOOLEAN", created["valueType"])
        assertEquals(50, (created["abRatio"] as Number).toInt())

        // List
        val list = restTemplate.getForEntity("/api/v1/admin/apps/$appId/configs", Map::class.java)
        assertEquals(HttpStatus.OK, list.statusCode)
        @Suppress("UNCHECKED_CAST")
        val configs = list.body!!["data"] as List<*>
        assertEquals(1, configs.size)

        // Update
        val update = restTemplate.exchange(
            "/api/v1/admin/apps/$appId/configs/$configId", HttpMethod.PUT,
            HttpEntity(
                UpdateConfigRequest(
                    configValue = "false", valueType = "BOOLEAN",
                    description = "Dark mode disabled", abRatio = 100, isActive = false,
                )
            ),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, update.statusCode)
        @Suppress("UNCHECKED_CAST")
        val updated = update.body!!["data"] as Map<String, Any>
        assertEquals("false", updated["configValue"])
        assertEquals(false, updated["isActive"])

        // Delete
        val delete = restTemplate.exchange(
            "/api/v1/admin/apps/$appId/configs/$configId", HttpMethod.DELETE,
            null, Void::class.java,
        )
        assertEquals(HttpStatus.NO_CONTENT, delete.statusCode)
    }

    // ─── Init with maintenance and notices ───────────────────

    @Test
    fun `init returns maintenance and notices when active`() {
        val (appId, appKey) = createApp()
        val now = OffsetDateTime.now()

        // Create active maintenance (current time window)
        restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/maintenances",
            CreateMaintenanceRequest(
                title = "Server Upgrade", message = "Back soon",
                startAt = now.minusHours(1), endAt = now.plusHours(1),
            ),
        )

        // Create active notice (current time window)
        restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/notices",
            CreateNoticeRequest(
                title = "Welcome", message = "Hello!", displayType = "ONCE",
                startAt = now.minusDays(1), endAt = now.plusDays(1),
            ),
        )

        // Create config
        restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/configs",
            CreateConfigRequest(configKey = "theme", configValue = "dark", valueType = "STRING"),
        )

        // Create version
        restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/versions",
            CreateVersionRequest(minVersion = "1.0.0", latestVersion = "2.0.0"),
        )

        // Call init
        @Suppress("UNCHECKED_CAST")
        val init = restTemplate.getForEntity(
            "/api/v1/init?app_key=$appKey&platform=ANDROID&app_version=1.5.0",
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, init.statusCode)
        val data = init.body!!["data"] as Map<String, Any>

        // Verify maintenance
        val maintenance = data["maintenance"] as Map<String, Any>
        assertEquals(true, maintenance["active"])
        assertEquals("Server Upgrade", maintenance["title"])

        // Verify update
        val update = data["update"] as Map<String, Any>
        assertEquals("2.0.0", update["latest_version"])

        // Verify notices
        val notices = data["notices"] as List<*>
        assertTrue(notices.isNotEmpty())

        // Verify config
        val config = data["config"] as Map<String, Any>
        assertEquals("dark", config["theme"])
    }
}
