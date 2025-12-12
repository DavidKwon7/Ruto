package com.handylab.ruto.domain.routine.usecase

import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.RoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRoutineListUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    operator fun invoke(): Flow<List<RoutineRead>> = repository.observeRoutineList()
}
