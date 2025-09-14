package com.example.ruto.auth

import android.app.Activity
import android.content.Context
import com.example.ruto.domain.IdTokenPayload
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Kakao
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException



class KakaoAuthProvider : AuthProvider {

    override val name: String = "Kakao"

    override suspend fun acquireIdToken(activity: Activity): IdTokenPayload {
        val token = suspendCancellableCoroutine<OAuthToken> { cont ->
            val cb: (OAuthToken?, Throwable?) -> Unit = { t, e ->
                if (e != null) cont.resumeWithException(e) else cont.resume(t!!)
            }
            val api = UserApiClient.instance
            if (api.isKakaoTalkLoginAvailable(activity)) api.loginWithKakaoTalk(activity, callback =  cb)
            else api.loginWithKakaoAccount(activity, callback =cb)
        }
        val id = token.idToken ?: error("카카오 OIDC 설정 필요(openid/email 동의)")
        return IdTokenPayload(idToken = id) // ↔ Supabase로 전달
    }
}
