package com.handylab.ruto.domain.routine.usecase

import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.RoutineRepository
import javax.inject.Inject

class FetchRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(id: String): Result<RoutineRead> = repository.fetchRoutine(id)
}
