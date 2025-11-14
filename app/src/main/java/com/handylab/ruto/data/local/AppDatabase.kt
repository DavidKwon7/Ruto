package com.handylab.ruto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.handylab.ruto.data.local.complete.PendingComplete
import com.handylab.ruto.data.local.complete.PendingCompleteDao
import com.handylab.ruto.data.local.routine.RoutineDao
import com.handylab.ruto.data.local.routine.RoutineEntity
import com.handylab.ruto.data.local.statistics.StatisticsDao
import com.handylab.ruto.data.local.statistics.StatisticsLocal

@Database(
    entities = [
        PendingComplete::class,
        RoutineCompletionLocal::class,
        StatisticsLocal::class,
        RoutineEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingCompleteDao(): PendingCompleteDao
    abstract fun routineCompletionDao(): RoutineCompletionDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun routineDao(): RoutineDao
}