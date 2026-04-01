package com.handylab.ruto.domain.profile.usecase

import com.handylab.ruto.domain.profile.ProfileRepository
import com.handylab.ruto.domain.profile.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ProfileUseCaseTest {

    private lateinit var repository: ProfileRepository

    private val sampleProfile = UserProfile(
        nickname = "홍길동",
        avatarUrl = "https://example.com/avatar.jpg",
        avatarVersion = 1
    )

    @Before
    fun setUp() {
        repository = mockk()
    }

    // ─────────────────────────────────────────────
    // LoadProfileUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `LoadProfile - 저장소에서 프로필을 반환한다`() = runTest {
        coEvery { repository.loadProfile() } returns sampleProfile

        val result = LoadProfileUseCase(repository)()

        assertEquals(sampleProfile, result)
    }

    @Test
    fun `LoadProfile - 저장소 호출이 정확히 1회 발생한다`() = runTest {
        coEvery { repository.loadProfile() } returns sampleProfile

        LoadProfileUseCase(repository)()

        coVerify(exactly = 1) { repository.loadProfile() }
    }

    @Test
    fun `LoadProfile - 저장소 예외 발생 시 예외가 전파된다`() = runTest {
        coEvery { repository.loadProfile() } throws RuntimeException("프로필 로드 실패")

        val exception = runCatching { LoadProfileUseCase(repository)() }.exceptionOrNull()

        assertEquals("프로필 로드 실패", exception?.message)
    }

    // ─────────────────────────────────────────────
    // UpdateNicknameUseCase
    // ─────────────────────────────────────────────

    @Test
    fun `UpdateNickname - 닉네임 업데이트 후 갱신된 프로필 반환한다`() = runTest {
        val updatedProfile = sampleProfile.copy(nickname = "새닉네임")
        coEvery { repository.updateNickname("새닉네임") } returns updatedProfile

        val result = UpdateNicknameUseCase(repository)("새닉네임")

        assertEquals("새닉네임", result.nickname)
    }

    @Test
    fun `UpdateNickname - 닉네임이 저장소로 그대로 전달된다`() = runTest {
        coEvery { repository.updateNickname(any()) } returns sampleProfile

        UpdateNicknameUseCase(repository)("테스트닉네임")

        coVerify(exactly = 1) { repository.updateNickname("테스트닉네임") }
    }

    @Test
    fun `UpdateNickname - 빈 문자열도 저장소로 전달된다`() = runTest {
        val emptyProfile = sampleProfile.copy(nickname = "")
        coEvery { repository.updateNickname("") } returns emptyProfile

        val result = UpdateNicknameUseCase(repository)("")

        assertEquals("", result.nickname)
    }

    @Test
    fun `UpdateNickname - 저장소 예외 발생 시 예외가 전파된다`() = runTest {
        coEvery { repository.updateNickname(any()) } throws RuntimeException("닉네임 업데이트 실패")

        val exception = runCatching { UpdateNicknameUseCase(repository)("닉네임") }.exceptionOrNull()

        assertEquals("닉네임 업데이트 실패", exception?.message)
    }
}
