package com.example.ruto.data.routine

import com.example.ruto.BuildConfig
import com.example.ruto.data.security.SecureStore
import com.example.ruto.domain.routine.RoutineCreateRequest
import com.example.ruto.domain.routine.RoutineCreateResponse
import com.example.ruto.domain.routine.RoutineDeleteRequest
import com.example.ruto.domain.routine.RoutineDeleteResponse
import com.example.ruto.domain.routine.RoutineListResponse
import com.example.ruto.domain.routine.RoutineRead
import com.example.ruto.domain.routine.RoutineUpdateRequest
import com.example.ruto.domain.routine.RoutineUpdateResponse
import com.example.ruto.util.AppLogger
import com.example.ruto.util.applyAuthHeaders
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineApi @Inject constructor(
    private val client: HttpClient,
    private val supabase: SupabaseClient,
    private val secure: SecureStore,
    private val logger: AppLogger
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

    suspend fun getRoutineList(): RoutineListResponse =
        client.get("$base/functions/v1/routines") {
            header("apikey", BuildConfig.SUPABASE_KEY)
            header(HttpHeaders.Accept, "application/json")
            applyAuthHeaders(supabase, secure)   // ✅ 공통 규칙 적용
        }.body()
    
    suspend fun getRoutine(id: String): RoutineRead {
        require(id.isNotBlank()) { "routine id is blank" }

        val resp = client.get("$base/functions/v1/routines/$id") {
            header("apikey", BuildConfig.SUPABASE_KEY)
            header(HttpHeaders.Accept, "application/json")
            applyAuthHeaders(supabase, secure) // Authorization or X-Guest-Id
        }
        val raw = resp.bodyAsText()
        logger.e("RoutineApi-getRoutine", raw)

        if (!resp.status.isSuccess()) {
            throw IllegalStateException("get-routine failed: ${resp.status} $raw")
        }
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }.decodeFromString(raw)
    }

    suspend fun updateRoutine(req: RoutineUpdateRequest): RoutineUpdateResponse =
        client.post("$base/functions/v1/update-routine") {
            header("apikey", BuildConfig.SUPABASE_KEY)
            applyAuthHeaders(supabase, secure)
            setBody(req) // null 필드 제외되어 전송됨(explicitNulls=false)
        }.body()

    suspend fun deleteRoutine(id: String): RoutineDeleteResponse =
        client.post("$base/functions/v1/delete-routine") {
            header("apikey", BuildConfig.SUPABASE_KEY)
            applyAuthHeaders(supabase, secure)
            setBody(RoutineDeleteRequest(id))
        }.body()
}
