package com.handylab.ruto.data.routine

import android.os.Build
import androidx.annotation.RequiresApi
import com.handylab.ruto.data.local.statistics.StatisticsDao
import com.handylab.ruto.data.local.statistics.StatisticsEntity
import com.handylab.ruto.data.statistics.LiveMonthlyStatsCalculator
import com.handylab.ruto.data.statistics.StatisticsCompletionsResponseDto
import com.handylab.ruto.data.statistics.toDto
import com.handylab.ruto.domain.routine.RoutineStatisticsRepository
import com.handylab.ruto.domain.routine.StatisticsCompletionsResponse
import com.handylab.ruto.util.AppLogger
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
class RoutineStatisticsRepositoryImpl @Inject constructor(
    private val api: RoutineApi,
    private val statisticsDao: StatisticsDao,
    private val supabase: SupabaseClient,
    private val json: Json,
    private val live: LiveMonthlyStatsCalculator,
    private val logger: AppLogger
) : RoutineStatisticsRepository {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun observeMonthly(tz: String, month: String): Flow<StatisticsCompletionsResponse> = channelFlow {
        live.observeMonthly(tz, month).onEach { send(it) }.launchIn(this)

        launch {
            runCatching { api.fetchMonthlyCompletions(tz, month) }
                .onSuccess { fresh ->
                    val dto = fresh.toDto()
                    val key = "$month|$tz|${currentScope()}|"
                    val payload = json.encodeToString(StatisticsCompletionsResponseDto.serializer(), dto)
                    statisticsDao.upsert(StatisticsEntity(key, payload, System.currentTimeMillis()))
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