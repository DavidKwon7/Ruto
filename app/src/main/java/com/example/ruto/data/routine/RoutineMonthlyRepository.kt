package com.example.ruto.data.routine

import com.example.ruto.data.local.statistics.StatisticsDao
import com.example.ruto.data.local.statistics.StatisticsLocal
import com.example.ruto.domain.routine.StatisticsCompletionsResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineMonthlyRepository @Inject constructor(
    private val api: RoutineApi,
    private val statisticsDao: StatisticsDao,
    private val supabase: SupabaseClient,
    private val json: Json
) {
    suspend fun getMonthly(
        tz: String,
        month: String,
        routineIdsCsv: String? = null,
        cacheTtlMs: Long = 30 * 60 * 1000L
    ): StatisticsCompletionsResponse {
        val scope = currentScope()
        val key = "$month|$tz|$scope|${routineIdsCsv ?: ""}"

        // 1) 캐시 히트
        statisticsDao.get(key)?.let { c ->
            if (System.currentTimeMillis() - c.updatedAt <= cacheTtlMs) {
                return json.decodeFromString(StatisticsCompletionsResponse.serializer(), c.payloadJson)
            }
        }

        // 2) 네트워크
        val fresh = api.fetchMonthlyCompletions(tz, month, routineIdsCsv)

        // 3) 캐시 저장
        val payload = json.encodeToString(StatisticsCompletionsResponse.serializer(), fresh)
        statisticsDao.upsert(
            StatisticsLocal(
                key = key,
                payloadJson = payload,
                updatedAt = System.currentTimeMillis()
            )
        )
        return fresh
    }

    private fun currentScope(): String {
        val uid = supabase.auth.currentUserOrNull()?.id
        return uid ?: "guest:${supabase.auth.currentSessionOrNull()?.user?.id ?: "anonymous"}"
    }
}