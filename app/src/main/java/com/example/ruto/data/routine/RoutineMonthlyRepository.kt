package com.example.ruto.data.routine

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ruto.data.local.statistics.StatisticsDao
import com.example.ruto.data.local.statistics.StatisticsLocal
import com.example.ruto.data.statistics.LiveMonthlyStatsCalculator
import com.example.ruto.data.statistics.model.StatisticsCompletionsResponse
import com.example.ruto.util.AppLogger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineMonthlyRepository @Inject constructor(
    private val api: RoutineApi,
    private val statisticsDao: StatisticsDao,
    private val supabase: SupabaseClient,
    private val json: Json,
    private val live: LiveMonthlyStatsCalculator,
    private val logger: AppLogger
) {
    /** 화면용: 1) 로컬 즉시 스트림  2) 백그라운드로 네트워크 가져와 캐시 갱신 */
    @RequiresApi(Build.VERSION_CODES.O)
    fun observeMonthly(tz: String, month: String): Flow<StatisticsCompletionsResponse> = channelFlow {
        // 1) 로컬 즉시 반영
        live.observeMonthly(tz, month).onEach { send(it) }.launchIn(this)

        // 2) 네트워크 최신분 가져와 캐시 저장(한 번)
        launch {
            runCatching { api.fetchMonthlyCompletions(tz, month) }
                .onSuccess { fresh ->
                    val key = "$month|$tz|${currentScope()}|"
                    val payload = json.encodeToString(StatisticsCompletionsResponse.serializer(), fresh)
                    statisticsDao.upsert(StatisticsLocal(key, payload, System.currentTimeMillis()))
                }
                .onFailure { it ->
                    logger.e("AuthRepository", "restore via refresh failed", it)
                }
        }
    }

    private fun currentScope(): String {
        val uid = supabase.auth.currentUserOrNull()?.id
        return uid ?: "guest:${supabase.auth.currentSessionOrNull()?.user?.id ?: "anonymous"}"
    }
}