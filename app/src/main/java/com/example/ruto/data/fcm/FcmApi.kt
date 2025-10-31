package com.example.ruto.data.fcm

import com.example.ruto.BuildConfig
import com.example.ruto.data.security.SecureStore
import com.example.ruto.domain.fcm.RegisterFcmModels
import com.example.ruto.util.applyAuthHeaders
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import javax.inject.Inject

class FcmApi @Inject constructor(
    private val client: HttpClient,
    private val supabase: SupabaseClient,
    private val secure: SecureStore
) {
    private val base = BuildConfig.SUPABASE_URL

    /**
     * Edge Function: /register-fcm
     * - Authorization: Bearer <access_token> (로그인)  또는
     * - X-Guest-Id: <uuid> (게스트)
     * - apikey: <ANON_KEY> (항상)
     */
    suspend fun registerFcmToken(
        req: RegisterFcmModels.RegisterFcmRequest,
        anonKey: String = BuildConfig.SUPABASE_KEY
    ): RegisterFcmModels.RegisterFcmResponse {
        val resp =  client.post("$base/functions/v1/register-fcm") {
            header("apikey", anonKey)
            applyAuthHeaders(supabase, secure)   // 공통 규칙 적용
            setBody(req)
        }
        if (!resp.status.isSuccess()) {
            throw IllegalStateException("register-fcm failed: ${resp.status} ${resp.bodyAsText()}")
        }
        return resp.body()
    }
}