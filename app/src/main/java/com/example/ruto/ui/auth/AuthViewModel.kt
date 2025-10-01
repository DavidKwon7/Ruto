package com.example.ruto.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruto.auth.AuthProvider
import com.example.ruto.data.auth.AuthRepository
import com.example.ruto.data.fcm.FcmApi
import com.example.ruto.data.fcm.RoutinePushHandler
import com.example.ruto.data.fcm.reRegisterFcm
import com.example.ruto.domain.AuthState
import com.example.ruto.domain.SocialProvider
import com.example.ruto.ui.event.UiEvent
import com.example.ruto.ui.state.UiState
import com.example.ruto.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val providers: List<@JvmSuppressWildcards AuthProvider>,
    private val logger: AppLogger,
    private val pushHandler: RoutinePushHandler,
    //private val fcmApi: FcmApi,
) : ViewModel() {
    val authState: StateFlow<AuthState> = repo.authState
    val bootstrapDone: StateFlow<Boolean> = repo.bootstrapDone

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _events = MutableSharedFlow<UiEvent>()
    val events: MutableSharedFlow<UiEvent> = _events

    init {
        viewModelScope.launch {
            // runCatching { reRegisterFcm(fcmApi) }
            bootstrapDone.filter { it }
                .take(1)    // 최초 1회
                .collect {
                    pushHandler.trySyncPendingToken()
                    pushHandler.onLoginOrBootstrap()
                }
        }
    }

    fun availableProviders(): List<AuthProvider> = providers

    fun signIn(activity: Activity, provider: AuthProvider) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                when (provider.name) {
                    "Google" -> {
                        val payload = provider.acquireIdToken(activity)
                        repo.signInWithIdToken(SocialProvider.Google, payload)
                            .onFailure { err ->
                                val msg = err.message ?: "Sign-in failed"
                                _uiState.update { s -> s.copy(error = msg) }
                                _events.emit(UiEvent.ShowSnackbar(msg))
                            }
                            .onSuccess {
                                // 로그인 성공 직후 FCM 토큰 재등록
                                // runCatching { reRegisterFcm(fcmApi) }

                                // 로그인 성공 직후: 유저 자격으로 FCM 재등록(게스트 토큰 승격)
                                viewModelScope.launch { pushHandler.onLoginOrBootstrap() }
                            }
                    }
                    "Kakao" -> {
                        // 브라우저 OAuth 시작 (세션 반영은 딥링크 콜백 후 자동)
                        provider.startOAuth(activity)
                        // Kakao는 콜백 후 세션이 생성되면 App 쪽에서
                        // bootstrapDone 또는 로그인 성공 신호에 맞춰 onLoginOrBootstrap() 호출됨
                    }
                    "Guest" -> {
                        repo.signInAsGuest()
                            .onFailure {
                                showMessage(it.message ?: "Guest sign-in failed")
                            }
                        // 게스트 모드일 때는 pushHandler.onLoginOrBootstrap()가 게스트 헤더(X-Guest-Id)로 등록 처리
                        viewModelScope.launch { pushHandler.onLoginOrBootstrap() }
                    }
                    else -> error("Unsupported provider: ${provider.name}")
                }
            } catch (e: Exception) {
                logger.e("AuthVM", "signIn(${provider.name}) fail", e)
                val msg = e.message ?: "Sign-in error"
                _uiState.update { it.copy(error = msg) }
                _events.emit(UiEvent.ShowSnackbar(msg))
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }


    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            repo.signOut()
                .onFailure {
                    showMessage(it.message ?: "Sign-out failed")
                }
            pushHandler.onLogout()
            _uiState.update { it.copy(loading = false) }
        }
    }

    private suspend fun showMessage(msg: String) {
        _uiState.update { it.copy(error = msg) }
        _events.emit(UiEvent.ShowSnackbar(msg))
    }
}