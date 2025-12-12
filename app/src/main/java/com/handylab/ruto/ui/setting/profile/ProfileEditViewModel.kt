package com.handylab.ruto.ui.setting.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handylab.ruto.domain.profile.DEFAULT_NICKNAME
import com.handylab.ruto.domain.profile.usecase.LoadProfileUseCase
import com.handylab.ruto.domain.profile.usecase.UpdateAvatarUseCase
import com.handylab.ruto.domain.profile.usecase.UpdateNicknameUseCase
import com.handylab.ruto.ui.setting.ProfileUiState
import com.handylab.ruto.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val loadProfileUseCase: LoadProfileUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
    private val logger: AppLogger
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(loading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // 새로 선택한 아바타 uri (서버엔 아직 업로드 안 된 상태)
    private var pendingAvatarUri: Uri? = null

    private var originalNickname: String = DEFAULT_NICKNAME
    private var originalAvatarUrl: String? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching { loadProfileUseCase() }
                .onSuccess { profile ->
                    originalNickname = profile.nickname
                    originalAvatarUrl = profile.avatarUrl
                    pendingAvatarUri = null

                    _uiState.value = ProfileUiState(
                        loading = false,
                        nickname = profile.nickname,
                        avatarUrl = profile.avatarUrl,
                        avatarVersion = profile.avatarVersion,
                        saving = false,
                        error = null
                    )
                }
                .onFailure { e ->
                    logger.e("ProfileEditVM", "loadProfile fail", e)
                    _uiState.value = ProfileUiState(
                        loading = false,
                        nickname = DEFAULT_NICKNAME,
                        avatarUrl = null,
                        saving = false,
                        error = e.message ?: "프로필을 불러오지 못했습니다."
                    )
                }
        }
    }

    fun onNicknameChange(text: String) {
        _uiState.update { it.copy(nickname = text, error = null) }
    }

    fun onAvatarSelected(uri: Uri) {
        pendingAvatarUri = uri
        _uiState.update { it.copy(avatarUrl = uri.toString(), error = null) }
    }

    fun onSave() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current.saving || current.loading) return@launch

            _uiState.update { it.copy(saving = true, error = null) }

            runCatching {
                var latestProfile = loadProfileUseCase()

                // 아바타가 변경된 경우
                pendingAvatarUri?.let { uri ->
                    latestProfile = updateAvatarUseCase(uri)
                    pendingAvatarUri = null
                }

                // 닉네임이 변경된 경우
                val trimmedNickname = current.nickname.trim().ifBlank { DEFAULT_NICKNAME }
                if (trimmedNickname != latestProfile.nickname) {
                    latestProfile = updateNicknameUseCase(trimmedNickname)
                }

                latestProfile
            }.onSuccess { profile ->
                originalNickname = profile.nickname
                originalAvatarUrl = profile.avatarUrl

                _uiState.value = ProfileUiState(
                    loading = false,
                    nickname = profile.nickname,
                    avatarUrl = profile.avatarUrl,
                    avatarVersion = profile.avatarVersion,
                    saving = false,
                    error = null
                )
            }.onFailure { e ->
                logger.e("ProfileEditVM", "save fail", e)
                _uiState.update {
                    it.copy(
                        saving = false,
                        error = e.message ?: "프로필 저장에 실패했습니다."
                    )
                }
            }
        }
    }
}