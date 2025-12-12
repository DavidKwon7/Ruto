package com.handylab.ruto.auth

import android.app.Activity
import com.handylab.ruto.domain.auth.IdTokenPayload

interface GoogleSignInStrategy {
    suspend fun acquireIdToken(activity: Activity): IdTokenPayload
}