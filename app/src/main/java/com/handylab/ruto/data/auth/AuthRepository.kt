package com.handylab.ruto.data.auth

import com.handylab.ruto.data.security.SecureStore
import com.handylab.ruto.domain.AuthState
import com.handylab.ruto.domain.IdTokenPayload
import com.handylab.ruto.domain.SocialProvider
import com.handylab.ruto.util.AppLogger
import com.handylab.ruto.util.withRetry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val secure: SecureStore,
    private val logger: AppLogger
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _bootstrapDone = MutableStateFlow(false)
    val bootstrapDone: StateFlow<Boolean> = _bootstrapDone

    private val KEY_REFRESH = "sb_refresh_token"
    private val KEY_GUEST = "guest_mode"

    init {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { supabase.auth.currentSessionOrNull() }
                .onSuccess { cur ->
                    if (cur != null) {
                        cur.refreshToken?.let { secure.putString(KEY_REFRESH, it) }
                        secure.clear(KEY_GUEST)
                        _authState.value = AuthState.SignedIn(
                            userId = cur.user?.id.orEmpty(),
                            email = cur.user?.email
                        )
                    } else {
                        val savedRefresh = secure.getString(KEY_REFRESH)
                        if (!savedRefresh.isNullOrBlank()) {
                            attemptRestoreWithRefresh(savedRefresh)
                        } else {
                            val guest = secure.getString(KEY_GUEST) == "1"
                            if (guest) {
                                _authState.value = AuthState.Guest
                            } else {
                                _authState.value = AuthState.SignedOut
                            }
                        }
                    }
                }
                .onFailure {
                    logger.e("AuthRepository", "restore session failed", it)
                    val guest = secure.getString(KEY_GUEST) == "1"
                    _authState.value = if (guest) AuthState.Guest else AuthState.SignedOut
                }

            _bootstrapDone.value = true

            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    SessionStatus.Initializing -> Unit
                    is SessionStatus.NotAuthenticated -> {
                        val guest = secure.getString(KEY_GUEST) == "1"
                        _authState.value = if (guest) AuthState.Guest else AuthState.SignedOut
                    }
                    is SessionStatus.Authenticated -> {
                        val s = status.session
                        s.refreshToken?.let { secure.putString(KEY_REFRESH, it) }
                        secure.clear(KEY_GUEST)
                        _authState.value = AuthState.SignedIn(
                            userId = s.user?.id.orEmpty(),
                            email = s.user?.email
                        )
                        logger.d("AuthRepository", "session authenticated: ${s.user?.id}")
                    }

                    is SessionStatus.RefreshFailure -> {
                        secure.clear(KEY_REFRESH)
                        val guest = secure.getString(KEY_GUEST) == "1"
                        _authState.value = if (guest) AuthState.Guest else AuthState.SignedOut
                    }
                }
            }
        }
    }

    private suspend fun attemptRestoreWithRefresh(refresh: String) {
        _authState.value = AuthState.Loading
        runCatching { supabase.auth.refreshSession(refresh) }
            .onSuccess { s ->
                s.refreshToken?.let { secure.putString(KEY_REFRESH, it) }
                secure.clear(KEY_GUEST)
                _authState.value = AuthState.SignedIn(
                    userId = s.user?.id.orEmpty(),
                    email = s.user?.email
                )
            }
            .onFailure {
                logger.e("AuthRepository", "restore via refresh failed", it)
                secure.clear(KEY_REFRESH)
                val guest = secure.getString(KEY_GUEST) == "1"
                _authState.value = if (guest) AuthState.Guest else AuthState.SignedOut
            }
    }

    suspend fun signInWithIdToken(
        provider: SocialProvider,
        payload: IdTokenPayload
    ): Result<Int> = runCatching {
        withRetry(maxAttempts = 3, initialDelayMs = 300) {
            when (provider) {
                SocialProvider.Google -> supabase.auth.signInWith(IDToken) {
                    idToken = payload.idToken
                    this.provider = Google
                    payload.rawNonce?.let { nonce = it }
                }

                SocialProvider.Kakao -> error("Kakao uses OAuth. Do not call signInWithIdToken.")

            }
        }
        val session = supabase.auth.currentSessionOrNull() ?: error("No session after sign-in")
        session.refreshToken?.let { secure.putString(KEY_REFRESH, it) }
        secure.clear(KEY_GUEST)
        logger.d("AuthRepository", "sign in success ${session.user?.id}")
    }.onFailure { exception ->
        logger.e("AuthRepository", "sign in failed", exception)
        _authState.value = AuthState.Error(exception.message)
    }

    /**
     * 게스트 로그인
     */
    suspend fun signInAsGuest(): Result<Int> = runCatching {
        secure.putString(KEY_GUEST, "1")
        secure.clear(KEY_REFRESH)
        _authState.value = AuthState.Guest
        logger.d("AuthRepository", "signInAsGuest success")
    }

    suspend fun signOut(): Result<Int> = runCatching {
        runCatching { supabase.auth.signOut() }
        secure.clear(KEY_REFRESH)
        secure.clear(KEY_GUEST)
        _authState.value = AuthState.SignedOut
        logger.d("AuthRepository", "signOut success")
    }.onFailure { e ->
        logger.e("AuthRepository", "signOut failed", e)
    }
}