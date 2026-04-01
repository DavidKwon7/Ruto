package com.handylab.ruto.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TimeUtilTest {

    private val seoulZone = ZoneId.of("Asia/Seoul")
    private val utcZone = ZoneId.of("UTC")

    // ─────────────────────────────────────────────
    // parseYYYYMMDD
    // ─────────────────────────────────────────────

    @Test
    fun `parseYYYYMMDD - 정상 날짜 문자열을 파싱한다`() {
        val result = parseYYYYMMDD("2025-03-15")
        assertEquals(LocalDate.of(2025, 3, 15), result)
    }

    @Test
    fun `parseYYYYMMDD - null 입력 시 null을 반환한다`() {
        assertNull(parseYYYYMMDD(null))
    }

    @Test
    fun `parseYYYYMMDD - 빈 문자열 입력 시 null을 반환한다`() {
        assertNull(parseYYYYMMDD(""))
    }

    @Test
    fun `parseYYYYMMDD - 공백만 있는 문자열 입력 시 null을 반환한다`() {
        assertNull(parseYYYYMMDD("   "))
    }

    @Test
    fun `parseYYYYMMDD - 잘못된 형식 입력 시 null을 반환한다`() {
        assertNull(parseYYYYMMDD("20250315"))      // 구분자 없음
        assertNull(parseYYYYMMDD("2025/03/15"))    // 슬래시 구분자
        assertNull(parseYYYYMMDD("15-03-2025"))    // 순서 역전
        assertNull(parseYYYYMMDD("not-a-date"))    // 완전 잘못된 값
    }

    @Test
    fun `parseYYYYMMDD - 존재하지 않는 날짜 입력 시 null을 반환한다`() {
        assertNull(parseYYYYMMDD("2025-13-01")) // 13월은 없음
        assertNull(parseYYYYMMDD("2025-02-30")) // 2월 30일은 없음
    }

    @Test
    fun `parseYYYYMMDD - 윤년 날짜를 올바르게 파싱한다`() {
        val result = parseYYYYMMDD("2024-02-29")
        assertEquals(LocalDate.of(2024, 2, 29), result)
    }

    @Test
    fun `parseYYYYMMDD - 비윤년의 2월 29일은 null을 반환한다`() {
        assertNull(parseYYYYMMDD("2025-02-29"))
    }

    // ─────────────────────────────────────────────
    // LocalDate.toEpochMillis
    // ─────────────────────────────────────────────

    @Test
    fun `toEpochMillis - UTC 기준 에포크 밀리초로 변환한다`() {
        // 1970-01-01 UTC = 0ms
        val date = LocalDate.of(1970, 1, 1)
        val millis = date.toEpochMillis(utcZone)
        assertEquals(0L, millis)
    }

    @Test
    fun `toEpochMillis - 타임존을 고려해 올바른 에포크 밀리초를 반환한다`() {
        // 2025-01-01 00:00:00 Asia/Seoul (UTC+9) = 2024-12-31 15:00:00 UTC
        val date = LocalDate.of(2025, 1, 1)
        val millisSeoul = date.toEpochMillis(seoulZone)
        val millisUtc = date.toEpochMillis(utcZone)

        // Seoul은 UTC보다 9시간 빠르므로 에포크는 9시간(32400000ms) 적다
        assertEquals(millisUtc - 9 * 3600 * 1000L, millisSeoul)
    }

    @Test
    fun `toEpochMillis - 변환된 밀리초를 다시 LocalDate로 복원할 수 있다`() {
        val original = LocalDate.of(2025, 6, 15)
        val millis = original.toEpochMillis(seoulZone)
        val restored = millisToLocalDate(millis, seoulZone)
        assertEquals(original, restored)
    }

    // ─────────────────────────────────────────────
    // millisToLocalDate
    // ─────────────────────────────────────────────

    @Test
    fun `millisToLocalDate - UTC 에포크 0은 1970-01-01을 반환한다`() {
        val result = millisToLocalDate(0L, utcZone)
        assertEquals(LocalDate.of(1970, 1, 1), result)
    }

    @Test
    fun `millisToLocalDate - 서울 타임존 기준으로 올바른 날짜를 반환한다`() {
        // 2025-01-01 00:00:00 Asia/Seoul의 에포크 밀리초
        val seoulMidnight = LocalDate.of(2025, 1, 1).toEpochMillis(seoulZone)
        val result = millisToLocalDate(seoulMidnight, seoulZone)
        assertEquals(LocalDate.of(2025, 1, 1), result)
    }

    @Test
    fun `millisToLocalDate - 같은 밀리초라도 타임존에 따라 날짜가 달라진다`() {
        // 2025-01-01 01:00:00 UTC = 2025-01-01 10:00:00 Seoul
        val utcJan1 = LocalDate.of(2025, 1, 1).toEpochMillis(utcZone) + 3600_000L

        val resultUtc = millisToLocalDate(utcJan1, utcZone)
        val resultSeoul = millisToLocalDate(utcJan1, seoulZone)

        // UTC 기준 2025-01-01, Seoul 기준도 2025-01-01 (UTC+9이므로 10시)
        assertEquals(LocalDate.of(2025, 1, 1), resultUtc)
        assertEquals(LocalDate.of(2025, 1, 1), resultSeoul)
    }

    // ─────────────────────────────────────────────
    // epochMillisToYYYYMMDD
    // ─────────────────────────────────────────────

    @Test
    fun `epochMillisToYYYYMMDD - UTC 에포크 0은 1970-01-01 문자열을 반환한다`() {
        val result = epochMillisToYYYYMMDD(0L, utcZone)
        assertEquals("1970-01-01", result)
    }

    @Test
    fun `epochMillisToYYYYMMDD - 올바른 YYYY-MM-DD 포맷으로 반환한다`() {
        val millis = LocalDate.of(2025, 3, 5).toEpochMillis(seoulZone)
        val result = epochMillisToYYYYMMDD(millis, seoulZone)
        assertEquals("2025-03-05", result)
    }

    @Test
    fun `epochMillisToYYYYMMDD 와 parseYYYYMMDD는 역함수 관계이다`() {
        val original = LocalDate.of(2025, 7, 20)
        val millis = original.toEpochMillis(seoulZone)
        val str = epochMillisToYYYYMMDD(millis, seoulZone)
        val parsed = parseYYYYMMDD(str)
        assertEquals(original, parsed)
    }
}
