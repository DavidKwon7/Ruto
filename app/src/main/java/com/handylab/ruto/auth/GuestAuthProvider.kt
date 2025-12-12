package com.handylab.ruto.auth

import android.app.Activity
import com.handylab.ruto.domain.auth.IdTokenPayload

class GuestAuthProvider : AuthProvider {
    override val name: String = "Guest"

    override suspend fun acquireIdToken(activity: Activity): IdTokenPayload {
        throw UnsupportedOperationException("Guest doesn't use ID Token")
    }

    override suspend fun startOAuth(activity: Activity) {
        throw UnsupportedOperationException("Guest doesn't use OAuth")
    }
}