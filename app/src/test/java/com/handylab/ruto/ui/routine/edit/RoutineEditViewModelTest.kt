package com.handylab.ruto.ui.routine.edit

import androidx.lifecycle.SavedStateHandle
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.RoutineUpdateRequest
import com.handylab.ruto.domain.routine.usecase.DeleteRoutineUseCase
import com.handylab.ruto.domain.routine.usecase.FetchRoutineUseCase
import com.handylab.ruto.domain.routine.usecase.UpdateRoutineUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fetchRoutineUseCase: FetchRoutineUseCase
    private lateinit var updateRoutineUseCase: UpdateRoutineUseCase
    private lateinit var deleteRoutineUseCase: DeleteRoutineUseCase

    private val routineId = "routine-abc"

    private val sampleRoutine = RoutineRead(
        id = routineId,
        name = "독서",
        cadence = RoutineCadence.DAILY,
        startDate = "2025-01-01",
        endDate = "2025-12-31",
        notifyEnabled = true,
        notifyTime = "08:00",
        timezone = "Asia/Seoul",
        tags = listOf("health")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fetchRoutineUseCase = mockk()
        updateRoutineUseCase = mockk()
        deleteRoutineUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): RoutineEditViewModel {
        val savedState = SavedStateHandle(mapOf("id" to routineId))
        return RoutineEditViewModel(
            fetchRoutineUseCase = fetchRoutineUseCase,
            updateRoutineUseCase = updateRoutineUseCase,
            deleteRoutineUseCase = deleteRoutineUseCase,
            savedState = savedState
        )
    }

    // ─────────────────────────────────────────────
    // 초기 로드
    // ─────────────────────────────────────────────

    @Test
    fun `초기 상태는 loading=true이다`() {
        coEvery { fetchRoutineUseCase(any()) } returns Result.success(sampleRoutine)
        val vm = createViewModel()
        assertTrue(vm.uiState.value.loading)
    }

    @Test
    fun `로드 성공 시 루틴 데이터가 state에 반영된다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.success(sampleRoutine)

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.loading)
        assertEquals("독서", state.name)
        assertEquals(RoutineCadence.DAILY, state.cadence)
        assertEquals("2025-01-01", state.startDate)
        assertEquals("2025-12-31", state.endDate)
        assertTrue(state.notifyEnabled)
        assertEquals("08:00", state.notifyTime)
        assertEquals("Asia/Seoul", state.timezone)
        assertEquals(listOf("health"), state.tags)
    }

    @Test
    fun `로드 실패 시 error 메시지가 설정된다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.failure(RuntimeException("루틴 없음"))

        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.loading)
        assertEquals("루틴 없음", vm.uiState.value.error)
    }

    // ─────────────────────────────────────────────
    // 필드 업데이트
    // ─────────────────────────────────────────────

    @Test
    fun `updateName - 이름이 업데이트된다`() = runTest {
        coEvery { fetchRoutineUseCase(any()) } returns Result.success(sampleRoutine)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateName("운동")
        assertEquals("운동", vm.uiState.value.name)
    }

    @Test
    fun `updateCadence - 주기가 업데이트된다`() = runTest {
        coEvery { fetchRoutineUseCase(any()) } returns Result.success(sampleRoutine)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateCadence(RoutineCadence.WEEKLY)
        assertEquals(RoutineCadence.WEEKLY, vm.uiState.value.cadence)
    }

    @Test
    fun `updateNotifyEnabled - 알림 활성화 상태가 업데이트된다`() = runTest {
        coEvery { fetchRoutineUseCase(any()) } returns Result.success(sampleRoutine)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateNotifyEnabled(false)
        assertFalse(vm.uiState.value.notifyEnabled)
    }

    @Test
    fun `updateTags - 중복 태그는 제거된다`() = runTest {
        coEvery { fetchRoutineUseCase(any()) } returns Result.success(sampleRoutine)
        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateTags(listOf("health", "health", "study"))
        assertEquals(listOf("health", "study"), vm.uiState.value.tags)
    }

    // ─────────────────────────────────────────────
    // save
    // ─────────────────────────────────────────────

    @Test
    fun `save 성공 시 saved=true가 된다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.success(sampleRoutine)
        coEvery { updateRoutineUseCase(any()) } returns Result.success(true)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.save()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saved)
    }

    @Test
    fun `save 성공 시 saving=false가 된다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.success(sampleRoutine)
        coEvery { updateRoutineUseCase(any()) } returns Result.success(true)

        val vm = createViewModel()
        advanceUntilIdle()
        vm.save()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.saving)
    }

    @Test
    fun `save 실패 시 error 메시지가 설정된다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.success(sampleRoutine)
        coEvery { updateRoutineUseCase(any()) } returns Result.failure(RuntimeException("저장 실패"))

        val vm = createViewModel()
        advanceUntilIdle()
        vm.save()
        advanceUntilIdle()

        assertEquals("저장 실패", vm.uiState.value.error)
        assertFalse(vm.uiState.value.saved)
    }

    @Test
    fun `save 시 현재 state의 값이 UpdateRoutineUseCase로 전달된다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.success(sampleRoutine)
        coEvery { updateRoutineUseCase(any()) } returns Result.success(true)

        val vm = createViewModel()
        advanceUntilIdle()
        vm.updateName("수정된 이름")
        vm.save()
        advanceUntilIdle()

        coVerify {
            updateRoutineUseCase(
                match { it.name == "수정된 이름" && it.id == routineId }
            )
        }
    }

    // ─────────────────────────────────────────────
    // delete
    // ─────────────────────────────────────────────

    @Test
    fun `delete 성공 시 onDone 콜백이 호출된다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.success(sampleRoutine)
        coEvery { deleteRoutineUseCase(routineId) } returns Result.success(true)

        val vm = createViewModel()
        advanceUntilIdle()

        var callbackCalled = false
        vm.delete { callbackCalled = true }
        advanceUntilIdle()

        assertTrue(callbackCalled)
    }

    @Test
    fun `delete 결과가 false이면 onDone 콜백이 호출되지 않는다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.success(sampleRoutine)
        coEvery { deleteRoutineUseCase(routineId) } returns Result.success(false)

        val vm = createViewModel()
        advanceUntilIdle()

        var callbackCalled = false
        vm.delete { callbackCalled = true }
        advanceUntilIdle()

        assertFalse(callbackCalled)
    }

    @Test
    fun `delete 실패 시 error 메시지가 설정된다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.success(sampleRoutine)
        coEvery { deleteRoutineUseCase(routineId) } returns Result.failure(RuntimeException("삭제 실패"))

        val vm = createViewModel()
        advanceUntilIdle()
        vm.delete {}
        advanceUntilIdle()

        assertEquals("삭제 실패", vm.uiState.value.error)
        assertFalse(vm.uiState.value.saving)
    }

    @Test
    fun `delete 성공 시 saving=false가 된다`() = runTest {
        coEvery { fetchRoutineUseCase(routineId) } returns Result.success(sampleRoutine)
        coEvery { deleteRoutineUseCase(routineId) } returns Result.success(true)

        val vm = createViewModel()
        advanceUntilIdle()
        vm.delete {}
        advanceUntilIdle()

        assertFalse(vm.uiState.value.saving)
    }
}
