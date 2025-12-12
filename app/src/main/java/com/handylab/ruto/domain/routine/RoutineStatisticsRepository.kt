package com.handylab.ruto.domain.routine

import kotlinx.coroutines.flow.Flow

interface RoutineStatisticsRepository {
    fun observeMonthly(tz: String, month: String): Flow<StatisticsCompletionsResponse>
}
