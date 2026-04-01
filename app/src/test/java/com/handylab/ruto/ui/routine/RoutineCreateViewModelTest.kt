package com.handylab.ruto.ui.routine

import app.cash.turbine.test
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineCreateResponse
import com.handylab.ruto.domain.routine.RoutineTag
import com.handylab.ruto.domain.routine.usecase.RegisterRoutineUseCase
import io.mockk.coEvery
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineCreateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var registerRoutineUseCase: RegisterRoutineUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        registerRoutineUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = RoutineCreateViewModel(registerRoutineUseCase)

    // ─────────────────────────────────────────────
    // 초기 상태
    // ─────────────────────────────────────────────

    @Test
    fun `초기 name은 빈 문자열이다`() {
        val vm = createViewModel()
        assertEquals("", vm.uiState.value.name)
    }

    @Test
    fun `초기 cadence는 DAILY이다`() {
        val vm = createViewModel()
        assertEquals(RoutineCadence.DAILY, vm.uiState.value.cadence)
    }

    @Test
    fun `초기 notifyEnabled는 false이다`() {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.notifyEnabled)
    }

    @Test
    fun `초기 savedId는 null이다`() {
        val vm = createViewModel()
        assertNull(vm.uiState.value.savedId)
    }

    // ─────────────────────────────────────────────
    // 필드 업데이트
    // ─────────────────────────────────────────────

    @Test
    fun `updateName - 이름이 업데이트된다`() {
        val vm = createViewModel()
        vm.updateName("매일 독서")
        assertEquals("매일 독서", vm.uiState.value.name)
    }

    @Test
    fun `updateCadence - 주기가 업데이트된다`() {
        val vm = createViewModel()
        vm.updateCadence(RoutineCadence.WEEKLY)
        assertEquals(RoutineCadence.WEEKLY, vm.uiState.value.cadence)
    }

    @Test
    fun `updateStartDate - 시작일이 업데이트된다`() {
        val vm = createViewModel()
        val date = LocalDate.of(2025, 6, 1)
        vm.updateStartDate(date)
        assertEquals(date, vm.uiState.value.startDate)
    }

    @Test
    fun `updateEndDate - 종료일이 업데이트된다`() {
        val vm = createViewModel()
        val date = LocalDate.of(2025, 12, 31)
        vm.updateEndDate(date)
        assertEquals(date, vm.uiState.value.endDate)
    }

    @Test
    fun `toggleNotify - true로 설정하면 notifyEnabled가 true가 된다`() {
        val vm = createViewModel()
        vm.toggleNotify(true)
        assertTrue(vm.uiState.value.notifyEnabled)
    }

    @Test
    fun `toggleNotify - false로 설정하면 notifyTime이 null이 된다`() {
        val vm = createViewModel()
        vm.updateNotifyTime("08:00")
        vm.toggleNotify(false)
        assertNull(vm.uiState.value.notifyTime)
    }

    @Test
    fun `toggleNotify - false로 설정해도 기존 notifyTime은 보존되다가 false가 되면 null이 된다`() {
        val vm = createViewModel()
        vm.toggleNotify(true)
        vm.updateNotifyTime("08:00")
        vm.toggleNotify(false)
        // 알림 끄면 notifyTime null
        assertNull(vm.uiState.value.notifyTime)
    }

    @Test
    fun `updateNotifyTime - 알림 시각이 업데이트된다`() {
        val vm = createViewModel()
        vm.updateNotifyTime("09:30")
        assertEquals("09:30", vm.uiState.value.notifyTime)
    }

    @Test
    fun `updateTags - 태그 목록이 업데이트된다`() {
        val vm = createViewModel()
        val tags = listOf(RoutineTag.Custom("운동"), RoutineTag.Custom("건강"))
        vm.updateTags(tags)
        assertEquals(tags, vm.uiState.value.tags)
    }

    // ─────────────────────────────────────────────
    // submit
    // ─────────────────────────────────────────────

    @Test
    fun `submit 성공 시 savedId가 설정된다`() = runTest {
        val vm = createViewModel()
        vm.updateName("운동")
        coEvery {
            registerRoutineUseCase(any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(RoutineCreateResponse(id = "new-id", createdAt = "2025-01-01T00:00:00Z"))

        vm.submit()
        advanceUntilIdle()

        assertEquals("new-id", vm.uiState.value.savedId)
    }

    @Test
    fun `submit 성공 시 loading이 false가 된다`() = runTest {
        val vm = createViewModel()
        coEvery {
            registerRoutineUseCase(any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(RoutineCreateResponse(id = "id", createdAt = "2025-01-01T00:00:00Z"))

        vm.submit()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.loading)
    }

    @Test
    fun `submit 실패 시 error 메시지가 설정된다`() = runTest {
        val vm = createViewModel()
        coEvery {
            registerRoutineUseCase(any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("등록 실패"))

        vm.submit()
        advanceUntilIdle()

        assertEquals("등록 실패", vm.uiState.value.error)
    }

    @Test
    fun `submit 실패 시 savedId는 null이다`() = runTest {
        val vm = createViewModel()
        coEvery {
            registerRoutineUseCase(any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("오류"))

        vm.submit()
        advanceUntilIdle()

        assertNull(vm.uiState.value.savedId)
    }

    @Test
    fun `submit 실패 시 loading이 false가 된다`() = runTest {
        val vm = createViewModel()
        coEvery {
            registerRoutineUseCase(any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(RuntimeException("오류"))

        vm.submit()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.loading)
    }

    @Test
    fun `submit 중에는 loading이 true이다`() = runTest {
        val vm = createViewModel()
        coEvery {
            registerRoutineUseCase(any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(RoutineCreateResponse(id = "id", createdAt = "2025-01-01T00:00:00Z"))

        vm.uiState.test {
            awaitItem() // 초기 상태

            vm.submit()
            val loadingState = awaitItem()
            assertTrue(loadingState.loading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submit 시 현재 name이 UseCase로 전달된다`() = runTest {
        val vm = createViewModel()
        vm.updateName("명상")
        var capturedName = ""
        coEvery {
            registerRoutineUseCase(
                name = capture(mutableListOf<String>().also { capturedName = it.firstOrNull() ?: "" }),
                cadence = any(), startDate = any(), endDate = any(),
                notifyEnabled = any(), notifyTime = any(), tags = any()
            )
        } answers {
            capturedName = firstArg()
            Result.success(RoutineCreateResponse(id = "id", createdAt = "2025-01-01T00:00:00Z"))
        }

        vm.submit()
        advanceUntilIdle()

        assertEquals("명상", capturedName)
    }
}
