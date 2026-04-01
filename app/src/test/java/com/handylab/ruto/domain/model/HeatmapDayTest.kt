package com.handylab.ruto.domain.model

import com.handylab.ruto.domain.routine.HeatmapDay
import org.junit.Assert.assertEquals
import org.junit.Test

class HeatmapDayTest {

    // ─────────────────────────────────────────────
    // safeCount
    // ─────────────────────────────────────────────

    @Test
    fun `safeCount - count가 total 이하이면 count를 반환한다`() {
        val day = HeatmapDay(date = "2025-01-01", count = 3, total = 5, percent = 60)
        assertEquals(3, day.safeCount)
    }

    @Test
    fun `safeCount - count가 total을 초과하면 total을 반환한다`() {
        val day = HeatmapDay(date = "2025-01-01", count = 7, total = 5, percent = 100)
        assertEquals(5, day.safeCount)
    }

    @Test
    fun `safeCount - total이 0이면 0을 반환한다`() {
        val day = HeatmapDay(date = "2025-01-01", count = 3, total = 0, percent = 0)
        assertEquals(0, day.safeCount)
    }

    @Test
    fun `safeCount - count와 total이 같으면 count를 반환한다`() {
        val day = HeatmapDay(date = "2025-01-01", count = 5, total = 5, percent = 100)
        assertEquals(5, day.safeCount)
    }

    @Test
    fun `safeCount - count가 0이면 0을 반환한다`() {
        val day = HeatmapDay(date = "2025-01-01", count = 0, total = 5, percent = 0)
        assertEquals(0, day.safeCount)
    }

    // ─────────────────────────────────────────────
    // safePercent
    // ─────────────────────────────────────────────

    @Test
    fun `safePercent - total이 0이면 0을 반환한다`() {
        val day = HeatmapDay(date = "2025-01-01", count = 0, total = 0, percent = 0)
        assertEquals(0, day.safePercent)
    }

    @Test
    fun `safePercent - 모두 완료하면 100을 반환한다`() {
        val day = HeatmapDay(date = "2025-01-01", count = 5, total = 5, percent = 100)
        assertEquals(100, day.safePercent)
    }

    @Test
    fun `safePercent - 절반 완료하면 50을 반환한다`() {
        val day = HeatmapDay(date = "2025-01-01", count = 2, total = 4, percent = 50)
        assertEquals(50, day.safePercent)
    }

    @Test
    fun `safePercent - count가 0이면 0을 반환한다`() {
        val day = HeatmapDay(date = "2025-01-01", count = 0, total = 5, percent = 0)
        assertEquals(0, day.safePercent)
    }

    @Test
    fun `safePercent - count가 total을 초과해도 100을 초과하지 않는다`() {
        // count=7, total=5 → safeCount=5 → percent=100
        val day = HeatmapDay(date = "2025-01-01", count = 7, total = 5, percent = 140)
        assertEquals(100, day.safePercent)
    }

    @Test
    fun `safePercent - 반올림이 올바르게 동작한다`() {
        // 1/3 = 33.33... → 33
        val day = HeatmapDay(date = "2025-01-01", count = 1, total = 3, percent = 33)
        assertEquals(33, day.safePercent)
    }

    @Test
    fun `safePercent - 2_3은 67로 반올림된다`() {
        // 2/3 = 66.67 → 67
        val day = HeatmapDay(date = "2025-01-01", count = 2, total = 3, percent = 67)
        assertEquals(67, day.safePercent)
    }

    @Test
    fun `safePercent - 항상 0과 100 사이 값이다`() {
        // 경계값 검증
        val zero = HeatmapDay(date = "2025-01-01", count = 0, total = 10, percent = 0)
        val full = HeatmapDay(date = "2025-01-01", count = 10, total = 10, percent = 100)
        val overflow = HeatmapDay(date = "2025-01-01", count = 100, total = 10, percent = 999)

        assertEquals(0, zero.safePercent)
        assertEquals(100, full.safePercent)
        assertEquals(100, overflow.safePercent)
    }
}
