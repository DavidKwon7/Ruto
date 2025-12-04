package com.handylab.ruto.domain

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AuthState {
    data object Loading : AuthState
    data class SignedIn(val userId: String, val email: String?) : AuthState
    data object SignedOut : AuthState
    data class Error(val message: String?) : AuthState

    data object Guest: AuthState
}

@Immutable
enum class SocialProvider { Google, Kakao }

@Immutable
data class IdTokenPayload(
    val idToken: String,
    val rawNonce: String? = null    // Google CM일 때만 사용
)