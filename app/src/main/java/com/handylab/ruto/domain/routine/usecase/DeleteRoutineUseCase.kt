package com.handylab.ruto.domain.routine.usecase

import com.handylab.ruto.domain.routine.RoutineRepository
import javax.inject.Inject

class DeleteRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(id: String): Result<Boolean> = repository.deleteRoutine(id)
}
