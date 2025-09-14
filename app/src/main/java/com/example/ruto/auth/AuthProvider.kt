package com.example.ruto.auth

import android.app.Activity
import android.content.Context
import com.example.ruto.domain.IdTokenPayload

interface AuthProvider {
    val name: String

    /**
     * ID Token 기반(구글 등)에서 사용
     */
    suspend fun acquireIdToken(activity: Activity): IdTokenPayload {
        throw UnsupportedOperationException("$name uses OAuth, not ID Token")
    }

    /**
     * OAuth 기반(카카오 등)에서 사용
     */
    suspend fun startOAuth(activity: Activity) {
        throw UnsupportedOperationException("$name uses ID Token, not OAuth")
    }
}