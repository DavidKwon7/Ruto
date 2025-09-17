package com.example.ruto.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ruto.auth.AuthProvider
import com.example.ruto.data.auth.AuthRepository
import com.example.ruto.domain.AuthState
import com.example.ruto.domain.SocialProvider
import com.example.ruto.ui.event.UiEvent
import com.example.ruto.ui.state.UiState
import com.example.ruto.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val providers: List<@JvmSuppressWildcards AuthProvider>,
    private val logger: AppLogger
) : ViewModel() {
    val authState: StateFlow<AuthState> = repo.authState
    val bootstrapDone: StateFlow<Boolean> = repo.bootstrapDone

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _events = MutableSharedFlow<UiEvent>()
    val events: MutableSharedFlow<UiEvent> = _events

    fun availableProviders(): List<AuthProvider> = providers

    fun signIn(activity: Activity, provider: AuthProvider) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                when (provider.name) {
                    "Google" -> {
                        val payload = provider.acquireIdToken(activity)
                        repo.signInWithIdToken(SocialProvider.Google, payload)
                            .onFailure {
                                showMessage(it.message ?: "Sign-in failed")
                            }
                    }
                    "Kakao" -> {
                        // 브라우저 OAuth 시작 (세션 반영은 딥링크 콜백 후 자동)
                        provider.startOAuth(activity)
                    }
                    "Guest" -> {
                        repo.signInAsGuest()
                            .onFailure {
                                showMessage(it.message ?: "Guest sign-in failed")
                            }
                    }
                    else -> error("Unsupported provider: ${provider.name}")
                }
            } catch (e: Exception) {
                logger.e("AuthVM", "signIn(${provider.name}) fail", e)
                showMessage(e.message ?: "Sign-in error")
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
            _uiState.update { it.copy(loading = false) }
        }
    }

    private suspend fun showMessage(msg: String) {
        _uiState.update { it.copy(error = msg) }
        _events.emit(UiEvent.ShowSnackbar(msg))
    }
}