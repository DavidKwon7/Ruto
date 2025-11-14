package com.handylab.ruto.domain

sealed interface AuthState {
    data object Loading : AuthState
    data class SignedIn(val userId: String, val email: String?) : AuthState
    data object SignedOut : AuthState
    data class Error(val message: String?) : AuthState

    data object Guest: AuthState
}

enum class SocialProvider { Google, Kakao }

data class IdTokenPayload(
    val idToken: String,
    val rawNonce: String? = null    // Google CM일 때만 사용
)