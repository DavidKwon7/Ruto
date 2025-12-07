package com.handylab.ruto.ui.setting

import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handylab.ruto.data.fcm.RoutinePushHandler
import com.handylab.ruto.data.profile.ProfileRepository
import com.handylab.ruto.data.setting.SettingRepository
import com.handylab.ruto.domain.profile.DEFAULT_NICKNAME
import com.handylab.ruto.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class PushUiState(
    val loading: Boolean,
    val enabled: Boolean,
    val error: String?,
    val needsPermission: Boolean,
)

@Immutable
data class ProfileUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val nickname: String = DEFAULT_NICKNAME,
    val avatarUrl: String? = null,
    val error: String? = null,
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val handler: RoutinePushHandler,
    private val settingRepository: SettingRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        PushUiState(
            loading = false,
            enabled = handler.getEnabledLocal(),
            error = null,
            needsPermission = false
        )
    )
    val uiState: StateFlow<PushUiState> = _uiState

    private val _profileUi = MutableStateFlow(ProfileUiState())
    val profileUi: StateFlow<ProfileUiState> = _profileUi

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: MutableSharedFlow<UiEvent> = _uiEvent

    val themeMode: StateFlow<ThemeMode> = settingRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    fun onToggleRequest(wantEnabled: Boolean, permissionGranted: Boolean) {
        if (wantEnabled && Build.VERSION.SDK_INT >= 33 && !permissionGranted) {
            _uiState.update { it.copy(needsPermission = true) }
            return
        }
        _uiState.update { it.copy(loading = true, error = null, needsPermission = false, enabled = wantEnabled) }

        viewModelScope.launch {
            runCatching { handler.togglePushEnabled(wantEnabled) }
                .onFailure { e ->
                    handler.setEnabledLocal(!wantEnabled)
                    _uiState.update { it.copy(loading = false, enabled = !wantEnabled, error = e.message ?: "네트워크 오류") }
                }
                .onSuccess {
                    _uiState.update { it.copy(loading = false) }
                }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingRepository.setThemeMode(mode)
        }
    }

    fun afterPermissionResult(granted: Boolean) {
        if (granted) {
            onToggleRequest(wantEnabled = true, permissionGranted = true)
        } else {
            _uiState.update { it.copy(needsPermission = false) }
        }
    }

    /**
     * 소셜 로그인을 진행한 경우만 가능
     * 게스트 로그인의 경우 불가능
     */
    fun loadProfile() {
        viewModelScope.launch {
            _profileUi.update { it.copy(loading = true, error = null) }

            runCatching {
                profileRepository.loadProfile()
            }.onSuccess { profile ->
                _profileUi.update {
                    it.copy(
                        loading = false,
                        nickname = profile.nickname.ifBlank { DEFAULT_NICKNAME },
                        avatarUrl = profile.avatarUrl,
                        error = null
                    )
                }
            }.onFailure { e ->
                _profileUi.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: "프로필을 불러오지 못했습니다."
                    )
                }
                _uiEvent.emit(UiEvent.ShowToastMsg(e.message ?: "프로필을 불러오지 못했습니다."))
            }
        }
    }

    fun onNicknameChange(newNickname: String) {
        _profileUi.update { it.copy(nickname = newNickname) }
    }

    fun saveNickname() {
        val nickname = profileUi.value.nickname

        viewModelScope.launch {
            _profileUi.update { it.copy(saving = true, error = null) }

            runCatching {
                profileRepository.updateNickname(nickname)
            }.onSuccess { profile ->
                _profileUi.update {
                    it.copy(
                        saving = false,
                        nickname = profile.nickname,
                        avatarUrl = profile.avatarUrl,
                        error = null
                    )
                }
            }.onFailure { e ->
                _profileUi.update {
                    it.copy(
                        saving = false,
                        error = e.message ?: "프로필 저장에 실패했습니다."
                    )
                }
                _uiEvent.emit(UiEvent.ShowToastMsg(e.message ?: "프로필 저장에 실패했습니다."))
            }
        }
    }

    fun onAvatarSelected(uri: Uri) {
        viewModelScope.launch {
            _profileUi.update { it.copy(saving = true, error = null) }

            runCatching {
                profileRepository.updateAvatar(uri)
            }.onSuccess { profile ->
                _profileUi.update {
                    it.copy(
                        saving = false,
                        nickname = profile.nickname,
                        avatarUrl = profile.avatarUrl,
                        error = null
                    )
                }
            }.onFailure { e ->
                _profileUi.update {
                    it.copy(
                        saving = false,
                        error = e.message ?: "프로필 이미지 업로드에 실패했습니다."
                    )
                }
                _uiEvent.emit(UiEvent.ShowToastMsg(e.message ?: "프로필 이미지 업로드에 실패했습니다."))
            }
        }
    }
}