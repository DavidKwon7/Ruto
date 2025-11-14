package com.handylab.ruto.auth

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.handylab.ruto.domain.IdTokenPayload
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LegacyGoogleSignInStrategy(
    private val webClientId: String
) : GoogleSignInStrategy {
    override suspend fun acquireIdToken(activity: Activity): IdTokenPayload {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId).requestEmail().build()
        val client = GoogleSignIn.getClient(activity, gso)

        val account = suspendCancellableCoroutine<GoogleSignInAccount> { cont ->
            val launcher = (activity as ComponentActivity).activityResultRegistry
                .register("google_sign_in", ActivityResultContracts.StartActivityForResult()) { r ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(r.data)
                    try { cont.resume(task.result) } catch (e: Exception) { cont.resumeWithException(e) }
                }
            activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) { launcher.unregister() }
            })
            launcher.launch(client.signInIntent)
        }
        val id = account.idToken ?: error("Google idToken is null")
        return IdTokenPayload(idToken = id)
    }
}