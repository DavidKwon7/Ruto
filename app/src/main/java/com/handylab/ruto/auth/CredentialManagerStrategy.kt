package com.handylab.ruto.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import java.security.MessageDigest
import java.util.UUID
import com.handylab.ruto.domain.IdTokenPayload
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
}