package com.verobee.vergate.unit

import com.verobee.vergate.common.exception.ApiException
import com.verobee.vergate.domain.model.DisplayType
import com.verobee.vergate.domain.model.Notice
import com.verobee.vergate.domain.service.NoticeService
import com.verobee.vergate.ports.out.GatewayCachePort
import com.verobee.vergate.ports.out.NoticeRepositoryPort
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class NoticeServiceTest {

    private val noticeRepository = mockk<NoticeRepositoryPort>()
    private val cache = mockk<GatewayCachePort>(relaxed = true)
    private val service = NoticeService(noticeRepository, cache)

    private val now = OffsetDateTime.now()
    private val APP_ID = "019500a0-0000-7000-8000-000000000100"
    private val NTC_ID1 = "019500a0-0000-7000-8000-000000000001"
    private val NTC_ID2 = "019500a0-0000-7000-8000-000000000002"

    private fun notice(id: String = NTC_ID1, appId: String = APP_ID) = Notice(
        id = id, appId = appId, title = "Test Notice", message = "Hello",
        imageUrl = "https://img.example.com/1.png", deepLink = "app://test",
        displayType = DisplayType.ONCE, priority = 5,
        startAt = now, endAt = now.plusDays(7),
    )

    @Test
    fun `create saves notice and evicts cache`() {
        val expected = notice()
        every { noticeRepository.save(any()) } returns expected

        val result = service.create(
            APP_ID, "Test Notice", "Hello", "https://img.example.com/1.png", "app://test",
            "ONCE", 5, now, now.plusDays(7),
        )

        assertEquals("Test Notice", result.title)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `update modifies notice and evicts cache`() {
        val existing = notice()
        every { noticeRepository.findById(NTC_ID1) } returns existing
        every { noticeRepository.save(any()) } answers { firstArg() }

        val result = service.update(
            NTC_ID1, "Updated", "New message", null, null,
            "DAILY", 10, now, now.plusDays(14), false,
        )

        assertEquals("Updated", result.title)
        assertEquals(DisplayType.DAILY, result.displayType)
        assertFalse(result.isActive)
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `delete removes notice and evicts cache`() {
        every { noticeRepository.findById(NTC_ID1) } returns notice()
        every { noticeRepository.deleteById(NTC_ID1) } just runs

        service.delete(NTC_ID1)

        verify { noticeRepository.deleteById(NTC_ID1) }
        verify { cache.evictByAppId(APP_ID) }
    }

    @Test
    fun `findById throws when not found`() {
        every { noticeRepository.findById("non-existent") } returns null

        assertThrows(ApiException::class.java) {
            service.findById("non-existent")
        }
    }

    @Test
    fun `findByAppId returns list`() {
        val notices = listOf(notice(NTC_ID1), notice(NTC_ID2))
        every { noticeRepository.findByAppId(APP_ID) } returns notices

        val result = service.findByAppId(APP_ID)

        assertEquals(2, result.size)
    }
}
