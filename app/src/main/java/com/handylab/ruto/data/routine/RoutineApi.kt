package com.handylab.ruto.data.routine

import com.handylab.ruto.BuildConfig
import com.handylab.ruto.data.routine.model.CompleteBatchRequestDto
import com.handylab.ruto.data.routine.model.CompleteBatchResponseDto
import com.handylab.ruto.data.routine.model.CompleteItemDto
import com.handylab.ruto.data.routine.model.RoutineCreateRequestDto
import com.handylab.ruto.data.routine.model.RoutineCreateResponseDto
import com.handylab.ruto.data.routine.model.RoutineDeleteRequestDto
import com.handylab.ruto.data.routine.model.RoutineDeleteResponseDto
import com.handylab.ruto.data.routine.model.RoutineListResponseDto
import com.handylab.ruto.data.routine.model.RoutineReadDto
import com.handylab.ruto.data.routine.model.RoutineUpdateRequestDto
import com.handylab.ruto.data.routine.model.RoutineUpdateResponseDto
import com.handylab.ruto.data.security.SecureStore
import com.handylab.ruto.data.statistics.model.StatisticsCompletionsResponseDto
import com.handylab.ruto.domain.routine.CompleteBatchRequest
import com.handylab.ruto.domain.routine.CompleteBatchResponse
import com.handylab.ruto.domain.routine.CompleteItem
import com.handylab.ruto.domain.routine.RoutineCreateRequest
import com.handylab.ruto.domain.routine.RoutineCreateResponse
import com.handylab.ruto.domain.routine.RoutineDeleteResponse
import com.handylab.ruto.domain.routine.RoutineListResponse
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.RoutineUpdateRequest
import com.handylab.ruto.domain.routine.RoutineUpdateResponse
import com.handylab.ruto.domain.routine.StatisticsCompletionsResponse
import com.handylab.ruto.util.AppLogger
import com.handylab.ruto.util.applyAuthHeaders
import io.github.jan.supabase.SupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
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
            setBody(RoutineCreateRequestDto.fromDomain(req))
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
        }.body<RoutineListResponseDto>().toDomain()
    
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
        }.decodeFromString<RoutineReadDto>(raw).toDomain()
    }

    suspend fun updateRoutine(req: RoutineUpdateRequest): RoutineUpdateResponse =
        client.post("$base/functions/v1/update-routine") {
            header("apikey", BuildConfig.SUPABASE_KEY)
            applyAuthHeaders(supabase, secure)
            setBody(RoutineUpdateRequestDto.fromDomain(req)) // null 필드 제외되어 전송됨(explicitNulls=false)
        }.body<RoutineUpdateResponseDto>().toDomain()

    suspend fun deleteRoutine(id: String): RoutineDeleteResponse =
        client.post("$base/functions/v1/delete-routine") {
            header("apikey", BuildConfig.SUPABASE_KEY)
            applyAuthHeaders(supabase, secure)
            setBody(RoutineDeleteRequestDto(id))
        }.body<RoutineDeleteResponseDto>().toDomain()

    suspend fun completeRoutinesBatch(items: List<CompleteItem>): CompleteBatchResponse =
        client.post("$base/functions/v1/complete-routines") {
            header("apikey", BuildConfig.SUPABASE_KEY)
            applyAuthHeaders(supabase, secure)
            setBody(CompleteBatchRequestDto.fromDomain(CompleteBatchRequest(items)))
        }.body<CompleteBatchResponseDto>().toDomain()

    suspend fun fetchMonthlyCompletions(
        tz: String,
        month: String = "current",        // "YYYY-MM" or "current"
        routineIdsCsv: String? = null
    ): StatisticsCompletionsResponse {
        val url = URLBuilder("$base/functions/v1/routine-completions-monthly").apply {
            parameters.append("tz", tz)
            parameters.append("month", month)
            routineIdsCsv?.let { parameters.append("routine_ids", it) }
        }.buildString()

        val resp = client.get(url) {
            header("apikey", BuildConfig.SUPABASE_KEY)
            header(HttpHeaders.Accept, "application/json")
            applyAuthHeaders(supabase, secure)
        }
        if (!resp.status.isSuccess()) {
            throw IllegalStateException("monthly fetch failed: ${resp.status} ${resp.body<String>()}")
        }
        return resp.body<StatisticsCompletionsResponseDto>().toDomain()
    }

}
