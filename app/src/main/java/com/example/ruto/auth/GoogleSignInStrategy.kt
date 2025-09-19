package com.example.ruto.auth

import android.app.Activity
import com.example.ruto.domain.IdTokenPayload

interface GoogleSignInStrategy {
    suspend fun acquireIdToken(activity: Activity): IdTokenPayload
}