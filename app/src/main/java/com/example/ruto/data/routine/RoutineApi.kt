package com.example.ruto.data.routine

import com.example.ruto.BuildConfig
import com.example.ruto.data.security.SecureStore
import com.example.ruto.domain.routine.RoutineCreateRequest
import com.example.ruto.domain.routine.RoutineCreateResponse
import com.example.ruto.util.applyAuthHeaders
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineApi @Inject constructor(
    private val client: HttpClient,
    private val supabase: SupabaseClient,
    private val secure: SecureStore
) {
    private val base = BuildConfig.SUPABASE_URL

    suspend fun createRoutine(
        req: RoutineCreateRequest,
        anonKey: String = BuildConfig.SUPABASE_KEY
    ): RoutineCreateResponse {
        val resp = client.post("$base/functions/v1/create-routine") {
            header("apikey", anonKey)
            applyAuthHeaders(supabase, secure)   // ✅ 공통 규칙 적용
            setBody(req)
        }
        if (!resp.status.isSuccess()) {
            val body = resp.bodyAsText()
            throw IllegalStateException("create-routine failed: ${resp.status} $body")
        }

        return resp.body()
    }

}
