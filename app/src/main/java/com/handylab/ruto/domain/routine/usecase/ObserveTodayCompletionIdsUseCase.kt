package com.handylab.ruto.domain.routine.usecase

import com.handylab.ruto.domain.routine.RoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTodayCompletionIdsUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeTodayCompletionIds()
}
