package com.handylab.ruto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.handylab.ruto.data.local.complete.PendingCompleteEntity
import com.handylab.ruto.data.local.complete.PendingCompleteDao
import com.handylab.ruto.data.local.routine.RoutineDao
import com.handylab.ruto.data.local.routine.RoutineEntity
import com.handylab.ruto.data.local.statistics.StatisticsDao
import com.handylab.ruto.data.local.statistics.StatisticsEntity

@Database(
    entities = [
        PendingCompleteEntity::class,
        RoutineCompletionEntity::class,
        StatisticsEntity::class,
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