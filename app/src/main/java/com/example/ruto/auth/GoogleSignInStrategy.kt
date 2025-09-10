package com.example.ruto.auth

import android.content.Context

interface GoogleSignInStrategy {
    fun isSignedIn(context: Context): Boolean
    fun signIn(context: Context, onResult: (String?, Exception?) -> Unit)
    fun signOut(context: Context)

}