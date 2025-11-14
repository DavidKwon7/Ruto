package com.handylab.ruto.auth

import android.app.Activity
import com.handylab.ruto.domain.IdTokenPayload
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Kakao
import javax.inject.Inject


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
