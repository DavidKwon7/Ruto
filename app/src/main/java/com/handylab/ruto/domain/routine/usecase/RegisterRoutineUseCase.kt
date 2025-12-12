package com.handylab.ruto.domain.routine.usecase

import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineCreateResponse
import com.handylab.ruto.domain.routine.RoutineRepository
import com.handylab.ruto.domain.routine.RoutineTag
import java.time.LocalDate
import javax.inject.Inject

class RegisterRoutineUseCase @Inject constructor(
    private val repository: RoutineRepository,
) {
    suspend operator fun invoke(
        name: String,
        cadence: RoutineCadence,
        startDate: LocalDate,
        endDate: LocalDate,
        notifyEnabled: Boolean,
        notifyTime: String?,
        tags: List<RoutineTag>,
    ): Result<RoutineCreateResponse> =
        repository.registerRoutine(name, cadence, startDate, endDate, notifyEnabled, notifyTime, tags)
}
