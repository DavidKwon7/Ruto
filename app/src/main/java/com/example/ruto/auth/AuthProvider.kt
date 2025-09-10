package com.example.ruto.auth

import android.content.Context

interface AuthProvider {
    val name: String
    fun isSignedIn(context: Context): Boolean
    fun signIn(context: Context, onResult: (String?, Exception?) -> Unit)
    fun signOut(context: Context)
}