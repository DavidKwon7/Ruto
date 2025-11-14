package com.handylab.ruto.data.local.statistics

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM statistics_cache WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): StatisticsLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: StatisticsLocal)

    @Query("DELETE FROM statistics_cache WHERE updatedAt < :expiredBefore")
    suspend fun purge(expiredBefore: Long)
}