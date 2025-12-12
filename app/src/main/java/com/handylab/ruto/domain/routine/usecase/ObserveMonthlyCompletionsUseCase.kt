package com.handylab.ruto.domain.routine.usecase

import com.handylab.ruto.domain.routine.RoutineStatisticsRepository
import com.handylab.ruto.domain.routine.StatisticsCompletionsResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMonthlyCompletionsUseCase @Inject constructor(
    private val repository: RoutineStatisticsRepository,
) {
    operator fun invoke(tz: String, month: String): Flow<StatisticsCompletionsResponse> =
        repository.observeMonthly(tz, month)
}
