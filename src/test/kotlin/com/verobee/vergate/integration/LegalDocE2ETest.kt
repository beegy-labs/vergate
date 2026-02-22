package com.verobee.vergate.integration

import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateAppRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.CreateLegalDocRequest
import com.verobee.vergate.adapters.`in`.rest.dto.admin.UpdateLegalDocRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LegalDocE2ETest : TestcontainersConfig() {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private fun createApp(): Pair<String, String> {
        val res = restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps",
            CreateAppRequest(name = "Legal Test App", platform = "WEB"),
        )
        @Suppress("UNCHECKED_CAST")
        val data = res.body!!["data"] as Map<String, Any>
        val appId = data["id"] as String
        val appKey = data["appKey"] as String
        return appId to appKey
    }

    @Test
    fun `legal doc CRUD - create, list, update, delete`() {
        val (appId, _) = createApp()

        // Create PRIVACY_POLICY (markdown)
        val create = restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/legal-docs",
            CreateLegalDocRequest(
                docType = "PRIVACY_POLICY",
                title = "개인정보처리방침",
                content = "## 제1조 (목적)\n본 방침은 개인정보 처리에 관한 사항을 규정합니다.",
                contentType = "MARKDOWN",
            ),
        )
        assertEquals(HttpStatus.CREATED, create.statusCode)
        @Suppress("UNCHECKED_CAST")
        val created = create.body!!["data"] as Map<String, Any>
        val docId = created["id"] as String
        assertEquals("PRIVACY_POLICY", created["docType"])
        assertEquals("MARKDOWN", created["contentType"])

        // List
        val list = restTemplate.getForEntity("/api/v1/admin/apps/$appId/legal-docs", Map::class.java)
        assertEquals(HttpStatus.OK, list.statusCode)
        @Suppress("UNCHECKED_CAST")
        val docs = list.body!!["data"] as List<*>
        assertEquals(1, docs.size)

        // Update to HTML
        val update = restTemplate.exchange(
            "/api/v1/admin/apps/$appId/legal-docs/$docId", HttpMethod.PUT,
            HttpEntity(UpdateLegalDocRequest(
                title = "개인정보처리방침 v2",
                content = "<h2>제1조 (목적)</h2><p>본 방침은 개인정보 처리에 관한 사항을 규정합니다.</p>",
                contentType = "HTML",
            )),
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, update.statusCode)
        @Suppress("UNCHECKED_CAST")
        val updated = update.body!!["data"] as Map<String, Any>
        assertEquals("개인정보처리방침 v2", updated["title"])
        assertEquals("HTML", updated["contentType"])

        // Delete
        val delete = restTemplate.exchange(
            "/api/v1/admin/apps/$appId/legal-docs/$docId", HttpMethod.DELETE,
            null, Void::class.java,
        )
        assertEquals(HttpStatus.NO_CONTENT, delete.statusCode)
    }

    @Test
    fun `markdown legal doc is rendered as HTML page`() {
        val (appId, appKey) = createApp()

        restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/legal-docs",
            CreateLegalDocRequest(
                docType = "PRIVACY_POLICY",
                title = "개인정보처리방침",
                content = "## 수집 항목\n- 이메일\n- 닉네임\n\n## 이용 목적\n서비스 제공을 위해 사용합니다.",
                contentType = "MARKDOWN",
            ),
        )

        val response = restTemplate.getForEntity(
            "/api/v1/legal/$appKey/privacy-policy",
            String::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.headers.contentType?.isCompatibleWith(MediaType.TEXT_HTML) == true)
        val html = response.body!!
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("개인정보처리방침"))
        assertTrue(html.contains("<h2>수집 항목</h2>"))   // markdown → HTML
        assertTrue(html.contains("<li>이메일</li>"))
    }

    @Test
    fun `HTML legal doc is served directly`() {
        val (appId, appKey) = createApp()

        restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/legal-docs",
            CreateLegalDocRequest(
                docType = "TERMS_OF_SERVICE",
                title = "이용약관",
                content = "<h2>제1조</h2><p>본 약관은 서비스 이용에 관한 사항을 규정합니다.</p>",
                contentType = "HTML",
            ),
        )

        val response = restTemplate.getForEntity(
            "/api/v1/legal/$appKey/terms",
            String::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        val html = response.body!!
        assertTrue(html.contains("<h2>제1조</h2>"))
        assertTrue(html.contains("이용약관"))
    }

    @Test
    fun `init response includes legal document URLs`() {
        val (appId, appKey) = createApp()

        restTemplate.postForEntity<Map<*, *>>(
            "/api/v1/admin/apps/$appId/legal-docs",
            CreateLegalDocRequest(
                docType = "PRIVACY_POLICY",
                title = "개인정보처리방침",
                content = "내용",
                contentType = "MARKDOWN",
            ),
        )

        @Suppress("UNCHECKED_CAST")
        val init = restTemplate.getForEntity(
            "/api/v1/init?app_key=$appKey&platform=WEB&app_version=1.0.0",
            Map::class.java,
        )
        assertEquals(HttpStatus.OK, init.statusCode)
        val data = init.body!!["data"] as Map<String, Any>
        val legal = data["legal"] as List<*>

        assertEquals(1, legal.size)
        @Suppress("UNCHECKED_CAST")
        val privacyLink = legal[0] as Map<String, Any>
        assertEquals("PRIVACY_POLICY", privacyLink["type"])
        assertEquals("개인정보처리방침", privacyLink["title"])
        assertTrue((privacyLink["url"] as String).contains("privacy-policy"))
    }
}
