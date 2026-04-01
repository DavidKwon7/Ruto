package com.handylab.ruto.ui.setting.profile

import android.net.Uri
import com.handylab.ruto.domain.profile.DEFAULT_NICKNAME
import com.handylab.ruto.domain.profile.UserProfile
import com.handylab.ruto.domain.profile.usecase.LoadProfileUseCase
import com.handylab.ruto.domain.profile.usecase.UpdateAvatarUseCase
import com.handylab.ruto.domain.profile.usecase.UpdateNicknameUseCase
import com.handylab.ruto.util.AppLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var loadProfileUseCase: LoadProfileUseCase
    private lateinit var updateNicknameUseCase: UpdateNicknameUseCase
    private lateinit var updateAvatarUseCase: UpdateAvatarUseCase
    private lateinit var logger: AppLogger

    private val sampleProfile = UserProfile(
        nickname = "홍길동",
        avatarUrl = "https://example.com/avatar.jpg",
        avatarVersion = 2
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        loadProfileUseCase = mockk()
        updateNicknameUseCase = mockk()
        updateAvatarUseCase = mockk()
        logger = mockk(relaxed = true) // 로그 호출은 무시
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProfileEditViewModel {
        return ProfileEditViewModel(
            loadProfileUseCase = loadProfileUseCase,
            updateNicknameUseCase = updateNicknameUseCase,
            updateAvatarUseCase = updateAvatarUseCase,
            logger = logger
        )
    }

    // ─────────────────────────────────────────────
    // 초기 로드 (init -> refresh)
    // ─────────────────────────────────────────────

    @Test
    fun `초기 상태는 loading=true이다`() {
        coEvery { loadProfileUseCase() } returns sampleProfile
        val vm = createViewModel()
        assertTrue(vm.uiState.value.loading)
    }

    @Test
    fun `refresh 성공 시 프로필 데이터가 state에 반영된다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.loading)
        assertEquals("홍길동", state.nickname)
        assertEquals("https://example.com/avatar.jpg", state.avatarUrl)
        assertEquals(2, state.avatarVersion)
        assertNull(state.error)
    }

    @Test
    fun `refresh 실패 시 error 메시지와 DEFAULT_NICKNAME이 설정된다`() = runTest {
        coEvery { loadProfileUseCase() } throws RuntimeException("네트워크 오류")

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.loading)
        assertEquals(DEFAULT_NICKNAME, state.nickname)
        assertEquals("네트워크 오류", state.error)
        assertNull(state.avatarUrl)
    }

    // ─────────────────────────────────────────────
    // onNicknameChange
    // ─────────────────────────────────────────────

    @Test
    fun `onNicknameChange - 닉네임과 error가 업데이트된다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onNicknameChange("새닉네임")

        assertEquals("새닉네임", vm.uiState.value.nickname)
        assertNull(vm.uiState.value.error)
    }

    // ─────────────────────────────────────────────
    // onAvatarSelected
    // ─────────────────────────────────────────────

    @Test
    fun `onAvatarSelected - avatarUrl이 Uri 문자열로 업데이트된다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile
        val vm = createViewModel()
        advanceUntilIdle()

        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://media/pick/0"

        vm.onAvatarSelected(uri)

        assertEquals("content://media/pick/0", vm.uiState.value.avatarUrl)
        assertNull(vm.uiState.value.error)
    }

    // ─────────────────────────────────────────────
    // onSave - 닉네임만 변경
    // ─────────────────────────────────────────────

    @Test
    fun `onSave - 닉네임 변경 시 UpdateNicknameUseCase가 호출된다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile
        val updatedProfile = sampleProfile.copy(nickname = "새닉네임")
        coEvery { updateNicknameUseCase("새닉네임") } returns updatedProfile

        val vm = createViewModel()
        advanceUntilIdle()
        vm.onNicknameChange("새닉네임")
        vm.onSave()
        advanceUntilIdle()

        coVerify(exactly = 1) { updateNicknameUseCase("새닉네임") }
    }

    @Test
    fun `onSave 성공 시 saved=true가 된다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile
        coEvery { updateNicknameUseCase(any()) } returns sampleProfile.copy(nickname = "수정")

        val vm = createViewModel()
        advanceUntilIdle()
        vm.onNicknameChange("수정")
        vm.onSave()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saved)
    }

    @Test
    fun `onSave 성공 시 saving=false가 된다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile
        coEvery { updateNicknameUseCase(any()) } returns sampleProfile

        val vm = createViewModel()
        advanceUntilIdle()
        vm.onSave()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.saving)
    }

    // ─────────────────────────────────────────────
    // onSave - 아바타 변경 포함
    // ─────────────────────────────────────────────

    @Test
    fun `onSave - 아바타 선택 시 UpdateAvatarUseCase가 호출된다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile
        val avatarUpdatedProfile = sampleProfile.copy(avatarUrl = "https://new.url/avatar.jpg", avatarVersion = 3)
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://media/pick/1"
        coEvery { updateAvatarUseCase(uri) } returns avatarUpdatedProfile

        val vm = createViewModel()
        advanceUntilIdle()
        vm.onAvatarSelected(uri)
        vm.onSave()
        advanceUntilIdle()

        coVerify(exactly = 1) { updateAvatarUseCase(uri) }
    }

    @Test
    fun `onSave - 아바타 업로드 후 닉네임도 다르면 둘 다 업데이트된다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://media/pick/2"
        val afterAvatarProfile = sampleProfile.copy(avatarUrl = "https://new.url/a.jpg", avatarVersion = 3)
        val finalProfile = afterAvatarProfile.copy(nickname = "변경닉네임")
        coEvery { updateAvatarUseCase(uri) } returns afterAvatarProfile
        coEvery { updateNicknameUseCase("변경닉네임") } returns finalProfile

        val vm = createViewModel()
        advanceUntilIdle()
        vm.onAvatarSelected(uri)
        vm.onNicknameChange("변경닉네임")
        vm.onSave()
        advanceUntilIdle()

        coVerify(exactly = 1) { updateAvatarUseCase(uri) }
        coVerify(exactly = 1) { updateNicknameUseCase("변경닉네임") }
        assertEquals("변경닉네임", vm.uiState.value.nickname)
    }

    // ─────────────────────────────────────────────
    // onSave - 닉네임 변경 없음 (trim 후 동일)
    // ─────────────────────────────────────────────

    @Test
    fun `onSave - 닉네임이 동일하면 UpdateNicknameUseCase를 호출하지 않는다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile

        val vm = createViewModel()
        advanceUntilIdle()
        // 닉네임을 변경하지 않고 저장
        vm.onSave()
        advanceUntilIdle()

        coVerify(exactly = 0) { updateNicknameUseCase(any()) }
    }

    @Test
    fun `onSave - 공백만 입력하면 DEFAULT_NICKNAME으로 저장 시도한다`() = runTest {
        // 공백 trim 후 blank면 DEFAULT_NICKNAME 사용
        // 서버의 기존 닉네임이 DEFAULT_NICKNAME이 아닐 때 → 업데이트 발생
        val profileWithDifferentNickname = sampleProfile.copy(nickname = "기존닉네임")
        coEvery { loadProfileUseCase() } returns profileWithDifferentNickname
        coEvery { updateNicknameUseCase(DEFAULT_NICKNAME) } returns sampleProfile.copy(nickname = DEFAULT_NICKNAME)

        val vm = createViewModel()
        advanceUntilIdle()
        vm.onNicknameChange("   ")
        vm.onSave()
        advanceUntilIdle()

        coVerify(exactly = 1) { updateNicknameUseCase(DEFAULT_NICKNAME) }
    }

    // ─────────────────────────────────────────────
    // onSave 실패
    // ─────────────────────────────────────────────

    @Test
    fun `onSave 실패 시 error 메시지가 설정된다`() = runTest {
        coEvery { loadProfileUseCase() } returns sampleProfile
        coEvery { updateNicknameUseCase(any()) } throws RuntimeException("저장 실패")

        val vm = createViewModel()
        advanceUntilIdle()
        vm.onNicknameChange("새닉네임")
        vm.onSave()
        advanceUntilIdle()

        assertEquals("저장 실패", vm.uiState.value.error)
        assertFalse(vm.uiState.value.saving)
        assertFalse(vm.uiState.value.saved)
    }

    // ─────────────────────────────────────────────
    // saving 중 중복 저장 방지
    // ─────────────────────────────────────────────

    @Test
    fun `loading 중에는 onSave가 무시된다`() = runTest {
        // loading=true 상태(init 직후, advanceUntilIdle 전)에서 onSave를 호출해도
        // updateNicknameUseCase, updateAvatarUseCase가 호출되지 않음을 검증
        coEvery { loadProfileUseCase() } returns sampleProfile

        val vm = createViewModel()
        vm.onNicknameChange("새닉네임")
        vm.onSave() // loading=true이므로 무시되어야 함
        advanceUntilIdle()

        coVerify(exactly = 0) { updateNicknameUseCase(any()) }
        coVerify(exactly = 0) { updateAvatarUseCase(any()) }
    }
}
