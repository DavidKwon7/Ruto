package com.example.ruto.auth

import android.app.Activity
import android.content.Context
import android.os.Build
import com.example.ruto.domain.IdTokenPayload

class GoogleAuthProvider(
    private val webClientId: String
) : AuthProvider {
    override val name: String = "Google"

    private val strategy: GoogleSignInStrategy =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            CredentialManagerStrategy(webClientId)
    } else {
            LegacyGoogleSignInStrategy(webClientId)
        }

    override suspend fun acquireIdToken(activity: Activity): IdTokenPayload =
        strategy.acquireIdToken(activity)

}