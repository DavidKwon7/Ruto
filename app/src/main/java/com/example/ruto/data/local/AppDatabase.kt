package com.example.ruto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ruto.data.local.complete.PendingComplete
import com.example.ruto.data.local.complete.PendingCompleteDao
import com.example.ruto.data.local.statistics.StatisticsDao
import com.example.ruto.data.local.statistics.StatisticsLocal

@Database(
    entities = [
        PendingComplete::class,
        RoutineCompletionLocal::class,
        StatisticsLocal::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingCompleteDao(): PendingCompleteDao
    abstract fun routineCompletionDao(): RoutineCompletionDao
    abstract fun statisticsDao(): StatisticsDao
}