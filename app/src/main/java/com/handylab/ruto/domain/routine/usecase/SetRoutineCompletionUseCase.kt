package com.handylab.ruto.domain.routine.usecase

import com.handylab.ruto.domain.routine.RoutineRepository
import javax.inject.Inject

class SetRoutineCompletionUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(routineId: String, completed: Boolean) =
        repository.setCompletionLocal(routineId, completed)
}
