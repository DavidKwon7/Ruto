package com.example.ruto.data.routine

import com.example.ruto.domain.routine.MonthlyCompletionsResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineMonthlyRepository @Inject constructor(
    private val api: RoutineApi,
    // private val monthlyDao: MonthlyDao,
    private val supabase: SupabaseClient,
    private val json: Json
) {
    suspend fun getMonthly(
        tz: String,
        month: String,
        routineIdsCsv: String? = null,
        cacheTtlMs: Long = 30 * 60 * 1000L
    ): MonthlyCompletionsResponse {
        val scope = currentScope()
        val key = "$month|$tz|$scope|${routineIdsCsv ?: ""}"

        // 1) 캐시 히트
        /*monthlyDao.get(key)?.let { c ->
            if (System.currentTimeMillis() - c.updatedAt <= cacheTtlMs) {
                return json.decodeFromString(MonthlyCompletionsResponse.serializer(), c.payloadJson)
            }
        }*/

        // 2) 네트워크
        val fresh = api.fetchMonthlyCompletions(tz, month, routineIdsCsv)

        // 3) 캐시 저장
        val payload = json.encodeToString(MonthlyCompletionsResponse.serializer(), fresh)
        /*monthlyDao.upsert(
            MonthlyCache(
                key = key,
                payloadJson = payload,
                updatedAt = System.currentTimeMillis()
            )
        )*/
        return fresh
    }

    private fun currentScope(): String {
        val uid = supabase.auth.currentUserOrNull()?.id
        return uid ?: "guest:${supabase.auth.currentSessionOrNull()?.user?.id ?: "anonymous"}"
    }
}