package com.verobee.vergate.unit

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.domain.model.LegalContentType
import com.verobee.vergate.domain.model.LegalDocType
import com.verobee.vergate.domain.model.LegalDocument
import com.verobee.vergate.domain.model.App
import com.verobee.vergate.domain.model.Platform
import com.verobee.vergate.domain.service.LegalDocumentService
import com.verobee.vergate.ports.out.AppRepositoryPort
import com.verobee.vergate.ports.out.GatewayCachePort
import com.verobee.vergate.ports.out.LegalDocumentRepositoryPort
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LegalDocumentServiceTest {

    private val legalDocRepository = mockk<LegalDocumentRepositoryPort>()
    private val appRepository = mockk<AppRepositoryPort>()
    private val cache = mockk<GatewayCachePort>(relaxed = true)
    private val service = LegalDocumentService(legalDocRepository, appRepository, cache)

    private val APP_ID = "019500a0-0000-7000-8000-000000000100"
    private val DOC_ID1 = "019500a0-0000-7000-8000-000000000001"
    private val DOC_ID2 = "019500a0-0000-7000-8000-000000000002"

    private val testApp = App(id = APP_ID, appKey = "my-app", name = "My App", platform = Platform.WEB)

    private fun doc(id: String = DOC_ID1) = LegalDocument(
        id = id, appId = APP_ID,
        docType = LegalDocType.PRIVACY_POLICY,
        title = "개인정보처리방침",
        content = "## 수집 항목\n이메일, 닉네임",
        contentType = LegalContentType.MARKDOWN,
    )

    @Test
    fun `create saves legal doc and evicts cache`() {
        every { legalDocRepository.existsByAppIdAndDocType(APP_ID, LegalDocType.PRIVACY_POLICY) } returns false
        every { legalDocRepository.save(any()) } returns doc()

        val result = service.create(APP_ID, "PRIVACY_POLICY", "개인정보처리방침", "## 수집 항목", "MARKDOWN")

        assertEquals("개인정보처리방침", result.title)
        assertEquals(LegalDocType.PRIVACY_POLICY, result.docType)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `create throws on duplicate doc type`() {
        every { legalDocRepository.existsByAppIdAndDocType(APP_ID, LegalDocType.PRIVACY_POLICY) } returns true

        assertThrows(ApiException::class.java) {
            service.create(APP_ID, "PRIVACY_POLICY", "title", "content", "MARKDOWN")
        }
    }

    @Test
    fun `update changes content and evicts cache`() {
        every { legalDocRepository.findById(DOC_ID1) } returns doc()
        every { legalDocRepository.save(any()) } answers { firstArg() }

        val result = service.update(DOC_ID1, "Updated Title", "<h1>Updated</h1>", "HTML", false)

        assertEquals("Updated Title", result.title)
        assertEquals(LegalContentType.HTML, result.contentType)
        assertFalse(result.isActive)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `delete removes doc and evicts cache`() {
        every { legalDocRepository.findById(DOC_ID1) } returns doc()
        every { legalDocRepository.deleteById(DOC_ID1) } just runs

        service.delete(DOC_ID1)

        verify { legalDocRepository.deleteById(DOC_ID1) }
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `findByAppKeyAndType returns doc`() {
        every { appRepository.findByAppKey("my-app") } returns testApp
        every { legalDocRepository.findByAppIdAndType(APP_ID, LegalDocType.PRIVACY_POLICY) } returns doc()

        val result = service.findByAppKeyAndType("my-app", LegalDocType.PRIVACY_POLICY)

        assertEquals("개인정보처리방침", result.title)
    }

    @Test
    fun `findByAppKeyAndType throws when app not found`() {
        every { appRepository.findByAppKey("unknown") } returns null

        assertThrows(ApiException::class.java) {
            service.findByAppKeyAndType("unknown", LegalDocType.PRIVACY_POLICY)
        }
    }

    @Test
    fun `findByAppKeyAndType throws when doc not found`() {
        every { appRepository.findByAppKey("my-app") } returns testApp
        every { legalDocRepository.findByAppIdAndType(APP_ID, LegalDocType.PRIVACY_POLICY) } returns null

        assertThrows(ApiException::class.java) {
            service.findByAppKeyAndType("my-app", LegalDocType.PRIVACY_POLICY)
        }
    }

    @Test
    fun `findByAppId returns all docs`() {
        every { legalDocRepository.findByAppId(APP_ID) } returns listOf(doc(DOC_ID1), doc(DOC_ID2))

        val result = service.findByAppId(APP_ID)

        assertEquals(2, result.size)
    }
}
