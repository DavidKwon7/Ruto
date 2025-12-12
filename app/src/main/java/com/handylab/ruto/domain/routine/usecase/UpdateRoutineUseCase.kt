package com.handylab.ruto.domain.routine.usecase

import com.handylab.ruto.domain.routine.RoutineRepository
import com.handylab.ruto.domain.routine.RoutineUpdateRequest
import javax.inject.Inject

class UpdateRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(request: RoutineUpdateRequest): Result<Boolean> =
        repository.updateRoutine(request)
}
