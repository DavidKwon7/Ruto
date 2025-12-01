package com.handylab.ruto.ui.setting

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.handylab.ruto.data.fcm.RoutinePushHandler
import com.handylab.ruto.data.setting.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PushUiState(
    val loading: Boolean,
    val enabled: Boolean,
    val error: String?,
    val needsPermission: Boolean
)

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val handler: RoutinePushHandler,
    private val settingRepository: SettingRepository,
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
}