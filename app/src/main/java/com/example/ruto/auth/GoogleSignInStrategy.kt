package com.example.ruto.auth

import android.app.Activity
import android.content.Context
import com.example.ruto.domain.IdTokenPayload

interface GoogleSignInStrategy {
    suspend fun acquireIdToken(activity: Activity): IdTokenPayload
    /*fun isSignedIn(context: Context): Boolean
    fun signIn(context: Context, onResult: (String?, Exception?) -> Unit)
    fun signOut(context: Context)*/

}