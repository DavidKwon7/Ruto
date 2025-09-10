package com.example.ruto.auth

import android.content.Context
import android.os.Build

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

    override fun isSignedIn(context: Context): Boolean {
        return strategy.isSignedIn(context)
    }

    override fun signIn(
        context: Context,
        onResult: (String?, Exception?) -> Unit
    ) {
        strategy.signIn(context, onResult)
    }

    override fun signOut(context: Context) {
        strategy.signOut(context)
    }
}