package com.example.ruto.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import androidx.core.content.edit
import com.example.ruto.domain.IdTokenPayload
import kotlinx.coroutines.withContext

class CredentialManagerStrategy(
    private val webClientId: String
) : GoogleSignInStrategy {

    override suspend fun acquireIdToken(
        activity: Activity
    ): IdTokenPayload = withContext(Dispatchers.IO) {
        val cm = CredentialManager.create(activity)
        val raw = UUID.randomUUID().toString()
        val hashed = MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setNonce(hashed) // Google에는 해시
            .build()

        val req = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val res = cm.getCredential(activity, req)
        val id = GoogleIdTokenCredential.createFrom(res.credential.data).idToken
        IdTokenPayload(idToken = id, rawNonce = raw) // Supabase엔 raw 전달
    }

    /*override fun isSignedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("google_token", null) != null
    }

    override fun signIn(
        context: Context,
        onResult: (String?, Exception?) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)
        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = MessageDigest.getInstance("SHA-256")
            .digest(rawNonce.toByteArray())
            .joinToString("") { "%02x".format(it) }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

                context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    .edit { putString("google_token", googleIdToken) }

                onResult(googleIdToken, null)
            } catch (e: Exception) {
                onResult(null, e)
            }
        }
    }

    override fun signOut(context: Context) {
        *//*context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            .edit().remove("google_token").apply()*//*
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            .edit { remove("google_token") }
    }*/

}