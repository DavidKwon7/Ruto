package com.handylab.ruto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineCompletionDao {

    @Query("""
        SELECT * FROM routine_completions
        WHERE ownerKey = :ownerKey AND date = :date
    """)
    fun observeByDate(ownerKey: String, date: String): Flow<List<RoutineCompletionEntity>>

    @Query("""
        SELECT * FROM routine_completions
        WHERE ownerKey = :ownerKey
          AND date BETWEEN :fromDate AND :toDate
    """)
    fun observeRange(
        ownerKey: String,
        fromDate: String, // "YYYY-MM-DD"
        toDate: String    // "YYYY-MM-DD"
    ): Flow<List<RoutineCompletionEntity>>

    @Query("""
        SELECT * FROM routine_completions
        WHERE ownerKey = :ownerKey AND date = :date AND routineId = :routineId
        LIMIT 1
    """)
    suspend fun getOne(ownerKey: String, date: String, routineId: String): RoutineCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RoutineCompletionEntity)

    @Query("""
        DELETE FROM routine_completions
        WHERE ownerKey = :ownerKey AND date < :keepFrom
    """)
    suspend fun purgeBefore(ownerKey: String, keepFrom: String)

    @Query("""
        DELETE FROM routine_completions
        WHERE ownerKey = :ownerKey AND routineId = :routineId AND date = :date
    """)
    suspend fun deleteOne(ownerKey: String, routineId: String, date: String)

    @Query("DELETE FROM routine_completions WHERE ownerKey = :ownerKey")
    suspend fun clearByOwner(ownerKey: String)
}