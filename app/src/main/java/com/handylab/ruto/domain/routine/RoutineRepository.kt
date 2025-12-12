package com.handylab.ruto.domain.routine

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RoutineRepository {
    fun observeRoutineList(): Flow<List<RoutineRead>>
    fun observeRoutine(id: String): Flow<RoutineRead?>
    fun observeTodayCompletionIds(): Flow<Set<String>>

    suspend fun refreshRoutines()
    suspend fun fetchRoutine(id: String): Result<RoutineRead>
    suspend fun registerRoutine(
        name: String,
        cadence: RoutineCadence,
        startDate: LocalDate,
        endDate: LocalDate,
        notifyEnabled: Boolean,
        notifyTime: String?,
        tags: List<RoutineTag>,
    ): Result<RoutineCreateResponse>

    suspend fun updateRoutine(request: RoutineUpdateRequest): Result<Boolean>
    suspend fun deleteRoutine(id: String): Result<Boolean>
    suspend fun setCompletionLocal(routineId: String, completed: Boolean)
}
