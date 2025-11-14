package com.handylab.ruto.workManager

import com.handylab.ruto.data.local.complete.PendingCompleteDao
import com.handylab.ruto.data.routine.RoutineApi
import com.handylab.ruto.util.AppLogger
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerDepsEntryPoint {
    fun pendingCompleteDao(): PendingCompleteDao
    fun routineApi(): RoutineApi
    fun appLogger(): AppLogger
}