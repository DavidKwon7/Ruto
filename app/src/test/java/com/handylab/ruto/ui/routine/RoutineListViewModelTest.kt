package com.handylab.ruto.ui.routine

import app.cash.turbine.test
import com.handylab.ruto.data.sync.CompleteQueue
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.usecase.ObserveRoutineListUseCase
import com.handylab.ruto.domain.routine.usecase.ObserveTodayCompletionIdsUseCase
import com.handylab.ruto.domain.routine.usecase.RefreshRoutinesUseCase
import com.handylab.ruto.domain.routine.usecase.SetRoutineCompletionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var observeRoutineListUseCase: ObserveRoutineListUseCase
    private lateinit var observeTodayCompletionIdsUseCase: ObserveTodayCompletionIdsUseCase
    private lateinit var refreshRoutinesUseCase: RefreshRoutinesUseCase
    private lateinit var setRoutineCompletionUseCase: SetRoutineCompletionUseCase
    private lateinit var queue: CompleteQueue

    private val routineListFlow = MutableStateFlow<List<RoutineRead>>(emptyList())
    private val completionIdsFlow = MutableStateFlow<Set<String>>(emptySet())

    private val routine1 = RoutineRead(
        id = "r1", name = "독서", cadence = RoutineCadence.DAILY,
        startDate = "2025-01-01", endDate = "2025-12-31",
        notifyEnabled = false, timezone = "Asia/Seoul"
    )
    private val routine2 = RoutineRead(
        id = "r2", name = "운동", cadence = RoutineCadence.DAILY,
        startDate = "2025-01-01", endDate = "2025-12-31",
        notifyEnabled = false, timezone = "Asia/Seoul"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        observeRoutineListUseCase = mockk()
        observeTodayCompletionIdsUseCase = mockk()
        refreshRoutinesUseCase = mockk()
        setRoutineCompletionUseCase = mockk()
        queue = mockk()

        every { observeRoutineListUseCase() } returns routineListFlow
        every { observeTodayCompletionIdsUseCase() } returns completionIdsFlow
        coEvery { refreshRoutinesUseCase() } returns Unit
        coEvery { setRoutineCompletionUseCase(any(), any()) } returns Unit
        coEvery { queue.enqueue(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = RoutineListViewModel(
        observeRoutineListUseCase = observeRoutineListUseCase,
        observeTodayCompletionIdsUseCase = observeTodayCompletionIdsUseCase,
        refreshRoutinesUseCase = refreshRoutinesUseCase,
        setRoutineCompletionUseCase = setRoutineCompletionUseCase,
        queue = queue
    )

    // ─────────────────────────────────────────────
    // 초기 상태
    // ─────────────────────────────────────────────

    @Test
    fun `초기 상태는 loading=true이다`() {
        val viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.loading)
    }

    @Test
    fun `루틴 목록이 emit되면 loading=false로 전환된다`() = runTest {
        val viewModel = createViewModel()

        routineListFlow.value = listOf(routine1)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
    }

    // ─────────────────────────────────────────────
    // combine 로직 - 목록과 완료 set 통합
    // ─────────────────────────────────────────────

    @Test
    fun `루틴 목록 emit 시 completedToday가 올바르게 매핑된다`() = runTest {
        val viewModel = createViewModel()

        completionIdsFlow.value = setOf("r1")
        routineListFlow.value = listOf(routine1, routine2)
        advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(2, items.size)
        assertTrue(items.first { it.routine.id == "r1" }.completedToday)
        assertFalse(items.first { it.routine.id == "r2" }.completedToday)
    }

    @Test
    fun `완료 set 변경 시 items가 재계산된다`() = runTest {
        val viewModel = createViewModel()
        routineListFlow.value = listOf(routine1, routine2)
        advanceUntilIdle()

        // r2 완료 추가
        completionIdsFlow.value = setOf("r2")
        advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertFalse(items.first { it.routine.id == "r1" }.completedToday)
        assertTrue(items.first { it.routine.id == "r2" }.completedToday)
    }

    @Test
    fun `루틴 목록이 비어있으면 items도 빈 리스트이다`() = runTest {
        val viewModel = createViewModel()
        routineListFlow.value = emptyList()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    // ─────────────────────────────────────────────
    // uiState Flow 연속 방출 검증 (Turbine)
    // ─────────────────────────────────────────────

    @Test
    fun `루틴 목록 변경 시 uiState가 새 items로 업데이트된다`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            // 초기 loading=true
            val initial = awaitItem()
            assertTrue(initial.loading)

            // 목록 emit
            routineListFlow.value = listOf(routine1)
            val updated = awaitItem()
            assertFalse(updated.loading)
            assertEquals(1, updated.items.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────────
    // toggleComplete
    // ─────────────────────────────────────────────

    @Test
    fun `toggleComplete - 미완료 루틴을 완료로 변경한다`() = runTest {
        val viewModel = createViewModel()
        routineListFlow.value = listOf(routine1)
        completionIdsFlow.value = emptySet()
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.first()
        viewModel.toggleComplete(item)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.items.first().completedToday)
    }

    @Test
    fun `toggleComplete - 완료된 루틴을 미완료로 변경한다`() = runTest {
        val viewModel = createViewModel()
        routineListFlow.value = listOf(routine1)
        completionIdsFlow.value = setOf("r1")
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.first()
        viewModel.toggleComplete(item)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.items.first().completedToday)
    }

    @Test
    fun `toggleComplete - 완료 시 SetRoutineCompletionUseCase를 true로 호출한다`() = runTest {
        val viewModel = createViewModel()
        routineListFlow.value = listOf(routine1)
        completionIdsFlow.value = emptySet()
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.first()
        viewModel.toggleComplete(item)
        advanceUntilIdle()

        coVerify(exactly = 1) { setRoutineCompletionUseCase("r1", true) }
    }

    @Test
    fun `toggleComplete - 완료 취소 시 SetRoutineCompletionUseCase를 false로 호출한다`() = runTest {
        val viewModel = createViewModel()
        routineListFlow.value = listOf(routine1)
        completionIdsFlow.value = setOf("r1")
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.first()
        viewModel.toggleComplete(item)
        advanceUntilIdle()

        coVerify(exactly = 1) { setRoutineCompletionUseCase("r1", false) }
    }

    @Test
    fun `toggleComplete - 완료 시 CompleteQueue에 enqueue한다`() = runTest {
        val viewModel = createViewModel()
        routineListFlow.value = listOf(routine1)
        completionIdsFlow.value = emptySet()
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.first()
        viewModel.toggleComplete(item)
        advanceUntilIdle()

        coVerify(exactly = 1) { queue.enqueue(routineId = "r1", completedAt = any()) }
    }

    @Test
    fun `toggleComplete - 완료 취소 시 CompleteQueue에 enqueue하지 않는다`() = runTest {
        val viewModel = createViewModel()
        routineListFlow.value = listOf(routine1)
        completionIdsFlow.value = setOf("r1")
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.first()
        viewModel.toggleComplete(item)
        advanceUntilIdle()

        coVerify(exactly = 0) { queue.enqueue(any(), any()) }
    }

    @Test
    fun `toggleComplete - 낙관적 업데이트로 즉시 UI에 반영된다`() = runTest {
        val viewModel = createViewModel()
        routineListFlow.value = listOf(routine1)
        completionIdsFlow.value = emptySet()
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.first()
        // toggleComplete 호출 직후(suspend 완료 전) 즉시 반영 확인
        viewModel.toggleComplete(item)

        assertTrue(viewModel.uiState.value.items.first().completedToday)
    }

    // ─────────────────────────────────────────────
    // refresh
    // ─────────────────────────────────────────────

    @Test
    fun `refresh - RefreshRoutinesUseCase를 호출한다`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        // init에서 1회 + refresh에서 1회 = 총 2회
        coVerify(atLeast = 2) { refreshRoutinesUseCase() }
    }
}
