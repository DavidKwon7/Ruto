package com.handylab.ruto.auth

import com.handylab.ruto.domain.AuthState

/**
 * 게스트 로그인 여부 판별
 */
val AuthState.isGuest: Boolean
    get() = this is AuthState.Guest

/**
 * 로그인한 유저의 userId 또는 null
 */
val AuthState.userIdOrNull: String?
    get() = when (this) {
        is AuthState.SignedIn -> this.userId
        else -> null
    }