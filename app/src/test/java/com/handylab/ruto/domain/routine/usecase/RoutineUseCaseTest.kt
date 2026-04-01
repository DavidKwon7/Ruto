package com.handylab.ruto.domain.routine.usecase

import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineCreateResponse
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.RoutineRepository
import com.handylab.ruto.domain.routine.RoutineTag
import com.handylab.ruto.domain.routine.RoutineUpdateRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class RoutineUseCaseTest {

    private lateinit var repository: RoutineRepository

    // 테스트용 RoutineRead 기본값
    private val sampleRoutine = RoutineRead(
        id = "routine-1",
        name = "매일 독서",
        cadence = RoutineCadence.DAILY,
        startDate = "2025-01-01",
        endDate = "2025-12-31",
        notifyEnabled = false,
        notifyTime = null,
        timezone = "Asia/Seoul",
        tags = emptyList()
    )

    @Before
    fun setUp() {
        repository = mockk()
    }

    // ─────────────────────────────────────────────
    // ObserveRoutineListUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `ObserveRoutineList - 저장소 플로우를 그대로 반환한다`() = runTest {
        val routines = listOf(sampleRoutine)
        every { repository.observeRoutineList() } returns flowOf(routines)

        val useCase = ObserveRoutineListUseCase(repository)
        val result = useCase().first()

        assertEquals(routines, result)
        verify(exactly = 1) { repository.observeRoutineList() }
    }

    @Test
    fun `ObserveRoutineList - 빈 목록도 정상 반환한다`() = runTest {
        every { repository.observeRoutineList() } returns flowOf(emptyList())

        val result = ObserveRoutineListUseCase(repository)().first()

        assertTrue(result.isEmpty())
    }

    // ─────────────────────────────────────────────
    // ObserveRoutineUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `ObserveRoutine - 존재하는 id에 대해 루틴을 반환한다`() = runTest {
        every { repository.observeRoutine("routine-1") } returns flowOf(sampleRoutine)

        val result = ObserveRoutineUseCase(repository)("routine-1").first()

        assertEquals(sampleRoutine, result)
    }

    @Test
    fun `ObserveRoutine - 존재하지 않는 id에 대해 null을 반환한다`() = runTest {
        every { repository.observeRoutine("unknown") } returns flowOf(null)

        val result = ObserveRoutineUseCase(repository)("unknown").first()

        assertNull(result)
    }

    // ─────────────────────────────────────────────
    // ObserveTodayCompletionIdsUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `ObserveTodayCompletionIds - 오늘 완료된 루틴 id Set을 반환한다`() = runTest {
        val doneSet = setOf("routine-1", "routine-3")
        every { repository.observeTodayCompletionIds() } returns flowOf(doneSet)

        val result = ObserveTodayCompletionIdsUseCase(repository)().first()

        assertEquals(doneSet, result)
    }

    @Test
    fun `ObserveTodayCompletionIds - 완료 항목 없으면 빈 Set 반환한다`() = runTest {
        every { repository.observeTodayCompletionIds() } returns flowOf(emptySet())

        val result = ObserveTodayCompletionIdsUseCase(repository)().first()

        assertTrue(result.isEmpty())
    }

    // ─────────────────────────────────────────────
    // RegisterRoutineUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `RegisterRoutine - 성공 시 Result_success 반환한다`() = runTest {
        val response = RoutineCreateResponse(id = "new-id", createdAt = "2025-01-01T00:00:00Z")
        coEvery {
            repository.registerRoutine(any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(response)

        val useCase = RegisterRoutineUseCase(repository)
        val result = useCase(
            name = "새 루틴",
            cadence = RoutineCadence.DAILY,
            startDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2025, 12, 31),
            notifyEnabled = false,
            notifyTime = null,
            tags = emptyList()
        )

        assertTrue(result.isSuccess)
        assertEquals("new-id", result.getOrNull()?.id)
    }

    @Test
    fun `RegisterRoutine - 저장소 실패 시 Result_failure 반환한다`() = runTest {
        coEvery {
            repository.registerRoutine(any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("서버 오류"))

        val result = RegisterRoutineUseCase(repository)(
            name = "실패 루틴",
            cadence = RoutineCadence.WEEKLY,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(1),
            notifyEnabled = false,
            notifyTime = null,
            tags = emptyList()
        )

        assertTrue(result.isFailure)
        assertEquals("서버 오류", result.exceptionOrNull()?.message)
    }

    @Test
    fun `RegisterRoutine - 파라미터가 저장소로 그대로 전달된다`() = runTest {
        val startDate = LocalDate.of(2025, 3, 1)
        val endDate = LocalDate.of(2025, 6, 30)
        val tags = listOf(RoutineTag.Custom("운동"))
        coEvery {
            repository.registerRoutine(
                name = "운동",
                cadence = RoutineCadence.DAILY,
                startDate = startDate,
                endDate = endDate,
                notifyEnabled = true,
                notifyTime = "07:00",
                tags = tags
            )
        } returns Result.success(RoutineCreateResponse("id-123", "2025-03-01T00:00:00Z"))

        RegisterRoutineUseCase(repository)(
            name = "운동",
            cadence = RoutineCadence.DAILY,
            startDate = startDate,
            endDate = endDate,
            notifyEnabled = true,
            notifyTime = "07:00",
            tags = tags
        )

        coVerify(exactly = 1) {
            repository.registerRoutine("운동", RoutineCadence.DAILY, startDate, endDate, true, "07:00", tags)
        }
    }

    // ─────────────────────────────────────────────
    // UpdateRoutineUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `UpdateRoutine - 성공 시 Result_success(true) 반환한다`() = runTest {
        val req = RoutineUpdateRequest(id = "routine-1", name = "수정된 이름")
        coEvery { repository.updateRoutine(req) } returns Result.success(true)

        val result = UpdateRoutineUseCase(repository)(req)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `UpdateRoutine - 저장소 실패 시 Result_failure 반환한다`() = runTest {
        val req = RoutineUpdateRequest(id = "routine-1", name = "수정")
        coEvery { repository.updateRoutine(req) } returns Result.failure(RuntimeException("업데이트 실패"))

        val result = UpdateRoutineUseCase(repository)(req)

        assertTrue(result.isFailure)
    }

    // ─────────────────────────────────────────────
    // DeleteRoutineUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `DeleteRoutine - 성공 시 Result_success(true) 반환한다`() = runTest {
        coEvery { repository.deleteRoutine("routine-1") } returns Result.success(true)

        val result = DeleteRoutineUseCase(repository)("routine-1")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
    }

    @Test
    fun `DeleteRoutine - 저장소 실패 시 Result_failure 반환한다`() = runTest {
        coEvery { repository.deleteRoutine("routine-1") } returns Result.failure(RuntimeException("삭제 실패"))

        val result = DeleteRoutineUseCase(repository)("routine-1")

        assertTrue(result.isFailure)
        assertEquals("삭제 실패", result.exceptionOrNull()?.message)
    }

    @Test
    fun `DeleteRoutine - id가 저장소로 그대로 전달된다`() = runTest {
        coEvery { repository.deleteRoutine(any()) } returns Result.success(true)

        DeleteRoutineUseCase(repository)("target-id")

        coVerify(exactly = 1) { repository.deleteRoutine("target-id") }
    }

    // ─────────────────────────────────────────────
    // FetchRoutineUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `FetchRoutine - 성공 시 루틴을 반환한다`() = runTest {
        coEvery { repository.fetchRoutine("routine-1") } returns Result.success(sampleRoutine)

        val result = FetchRoutineUseCase(repository)("routine-1")

        assertTrue(result.isSuccess)
        assertEquals(sampleRoutine, result.getOrNull())
    }

    @Test
    fun `FetchRoutine - 존재하지 않는 루틴 요청 시 실패 반환한다`() = runTest {
        coEvery { repository.fetchRoutine("unknown") } returns Result.failure(RuntimeException("루틴 없음"))

        val result = FetchRoutineUseCase(repository)("unknown")

        assertTrue(result.isFailure)
    }

    // ─────────────────────────────────────────────
    // SetRoutineCompletionUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `SetRoutineCompletion - completed=true 로 저장소 호출한다`() = runTest {
        coEvery { repository.setCompletionLocal("routine-1", true) } returns Unit

        SetRoutineCompletionUseCase(repository)("routine-1", true)

        coVerify(exactly = 1) { repository.setCompletionLocal("routine-1", true) }
    }

    @Test
    fun `SetRoutineCompletion - completed=false 로 저장소 호출한다`() = runTest {
        coEvery { repository.setCompletionLocal("routine-1", false) } returns Unit

        SetRoutineCompletionUseCase(repository)("routine-1", false)

        coVerify(exactly = 1) { repository.setCompletionLocal("routine-1", false) }
    }

    // ─────────────────────────────────────────────
    // RefreshRoutinesUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `RefreshRoutines - 저장소의 refreshRoutines를 호출한다`() = runTest {
        coEvery { repository.refreshRoutines() } returns Unit

        RefreshRoutinesUseCase(repository)()

        coVerify(exactly = 1) { repository.refreshRoutines() }
    }
}
