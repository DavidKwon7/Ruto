package com.example.ruto.auth

import android.app.Activity
import android.content.Context
import com.example.ruto.domain.IdTokenPayload

interface AuthProvider {
    val name: String
    suspend fun acquireIdToken(activity: Activity): IdTokenPayload
}