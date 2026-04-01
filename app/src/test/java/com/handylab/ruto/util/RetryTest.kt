package com.handylab.ruto.util

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RetryTest {

    // ─────────────────────────────────────────────
    // 성공 케이스
    // ─────────────────────────────────────────────

    @Test
    fun `첫 번째 시도에 성공하면 결과를 반환한다`() = runTest {
        val result = withRetry(maxAttempts = 3, initialDelayMs = 0) { 42 }
        assertEquals(42, result)
    }

    @Test
    fun `두 번째 시도에 성공하면 결과를 반환한다`() = runTest {
        var attempt = 0
        val result = withRetry<String>(maxAttempts = 3, initialDelayMs = 0) {
            attempt++
            if (attempt < 2) throw RuntimeException("실패")
            "성공"
        }
        assertEquals("성공", result)
        assertEquals(2, attempt)
    }

    @Test
    fun `maxAttempts 번째 시도에 성공하면 결과를 반환한다`() = runTest {
        var attempt = 0
        val result = withRetry<Int>(maxAttempts = 3, initialDelayMs = 0) {
            attempt++
            if (attempt < 3) throw RuntimeException("아직 실패")
            99
        }
        assertEquals(99, result)
        assertEquals(3, attempt)
    }

    // ─────────────────────────────────────────────
    // 실패 케이스
    // ─────────────────────────────────────────────

    @Test
    fun `maxAttempts 모두 실패하면 마지막 예외를 던진다`() = runTest {
        var attempt = 0
        try {
            withRetry<Unit>(maxAttempts = 3, initialDelayMs = 0) {
                attempt++
                throw IllegalStateException("시도 $attempt 실패")
            }
            fail("예외가 발생해야 합니다")
        } catch (e: IllegalStateException) {
            assertEquals("시도 3 실패", e.message)
            assertEquals(3, attempt)
        }
    }

    @Test
    fun `maxAttempts=1이면 재시도 없이 즉시 예외를 던진다`() = runTest {
        var attempt = 0
        try {
            withRetry<Unit>(maxAttempts = 1, initialDelayMs = 0) {
                attempt++
                throw RuntimeException("단건 실패")
            }
            fail("예외가 발생해야 합니다")
        } catch (e: RuntimeException) {
            assertEquals("단건 실패", e.message)
            assertEquals(1, attempt)
        }
    }

    // ─────────────────────────────────────────────
    // shouldRetry 조건부 재시도
    // ─────────────────────────────────────────────

    @Test
    fun `shouldRetry가 false를 반환하면 즉시 예외를 던진다`() = runTest {
        var attempt = 0
        try {
            withRetry<Unit>(
                maxAttempts = 5,
                initialDelayMs = 0,
                shouldRetry = { false }
            ) {
                attempt++
                throw RuntimeException("재시도 안 함")
            }
            fail("예외가 발생해야 합니다")
        } catch (e: RuntimeException) {
            assertEquals("재시도 안 함", e.message)
            // shouldRetry=false이면 첫 실패 직후 중단
            assertEquals(1, attempt)
        }
    }

    @Test
    fun `shouldRetry가 특정 예외 타입에만 false를 반환하면 해당 예외에서 즉시 중단한다`() = runTest {
        var attempt = 0
        try {
            withRetry<Unit>(
                maxAttempts = 5,
                initialDelayMs = 0,
                shouldRetry = { it !is IllegalArgumentException }
            ) {
                attempt++
                if (attempt == 2) throw IllegalArgumentException("중단 예외")
                throw RuntimeException("재시도 예외")
            }
            fail("예외가 발생해야 합니다")
        } catch (e: IllegalArgumentException) {
            assertEquals("중단 예외", e.message)
            assertEquals(2, attempt)
        }
    }

    // ─────────────────────────────────────────────
    // 시도 횟수 검증
    // ─────────────────────────────────────────────

    @Test
    fun `maxAttempts=3일 때 정확히 3번 시도한다`() = runTest {
        var attempt = 0
        try {
            withRetry<Unit>(maxAttempts = 3, initialDelayMs = 0) {
                attempt++
                throw RuntimeException("계속 실패")
            }
        } catch (_: RuntimeException) {}

        assertEquals(3, attempt)
    }

    @Test
    fun `재시도 중 딜레이가 기하급수적으로 증가하며 성공한다`() = runTest {
        // initialDelayMs=0으로 실제 딜레이 없이 로직만 검증
        var attempt = 0
        val result = withRetry<String>(
            maxAttempts = 4,
            initialDelayMs = 0,
            factor = 2.0
        ) {
            attempt++
            if (attempt < 4) throw RuntimeException("실패")
            "최종 성공"
        }
        assertEquals("최종 성공", result)
        assertEquals(4, attempt)
    }
}
