package com.example.ruto.auth

import android.app.Activity
import android.content.Context
import com.example.ruto.domain.IdTokenPayload
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Kakao
import io.github.jan.supabase.auth.providers.OAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class KakaoAuthProvider @Inject constructor(
    private val supabase: SupabaseClient
) : AuthProvider {

    override val name: String = "Kakao"

    // ID Token 경로는 더 이상 사용하지 않음
    override suspend fun acquireIdToken(activity: Activity): IdTokenPayload {
        throw UnsupportedOperationException("Kakao uses OAuth. Call startOAuth() instead.")
    }

    override suspend fun startOAuth(activity: Activity) {
        supabase.auth.signInWith(Kakao)
    }
}
