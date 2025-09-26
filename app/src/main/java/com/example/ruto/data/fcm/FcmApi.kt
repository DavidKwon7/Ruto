package com.example.ruto.data.fcm

import com.example.ruto.domain.fcm.RegisterFcmModels
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import javax.inject.Inject

class FcmApi @Inject constructor(
    private val client: HttpClient,
    private val supabase: SupabaseClient
) {
    private val functionsBase = "https://wyqbynrmzndxuiahhdxg.functions.supabase.co"

    /**
     * Edge Function: /register-fcm
     * - Authorization: Bearer <access_token> (로그인 사용자면)
     * - X-Guest-Id: <uuid> (게스트면)
     * - apikey: <ANON_KEY> (항상)
     */
    suspend fun registerFcmToken(
        req: RegisterFcmModels.RegisterFcmRequest,
        anonKey: String,
        guestId: String?
    ): RegisterFcmModels.RegisterFcmResponse {
        val accessToken = supabase.auth.currentSessionOrNull()?.accessToken
        return client.post("$functionsBase/register-fcm") {
            setBody(req)
            header("apikey", anonKey)
            accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            guestId?.let { header("X-Guest-Id", it) }
        }.body()
    }
}