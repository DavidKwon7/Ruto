package com.handylab.ruto.data.local.complete

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingCompleteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PendingComplete>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PendingComplete)

    @Query("SELECT * FROM pending_completes LIMIT :limit")
    suspend fun loadBatch(limit: Int = 50): List<PendingComplete>

    @Query("DELETE FROM pending_completes WHERE opId IN (:opIds)")
    suspend fun deleteByOpIds(opIds: List<String>)

    @Query("DELETE FROM pending_completes")
    suspend fun clear()
}