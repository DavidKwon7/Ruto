package com.example.ruto.workManager

import com.example.ruto.data.local.complete.PendingCompleteDao
import com.example.ruto.data.routine.RoutineApi
import com.example.ruto.util.AppLogger
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