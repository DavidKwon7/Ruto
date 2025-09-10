package com.example.ruto.auth

import android.app.Activity
import android.content.Context
import com.example.ruto.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LegacyGoogleSignInStrategy(
    private val webClientId: String
) : GoogleSignInStrategy {
    override fun isSignedIn(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && !account.isExpired
    }

    override fun signIn(
        context: Context,
        onResult: (String?, Exception?) -> Unit
    ) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        val activity = context as Activity
        activity.startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun signOut(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(context, gso).signOut()
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}