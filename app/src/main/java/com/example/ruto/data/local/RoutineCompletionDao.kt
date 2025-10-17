package com.example.ruto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineCompletionDao {

    @Query("SELECT * FROM routine_completions WHERE date = :date")
    fun observeByDate(date: String): Flow<List<RoutineCompletionLocal>>

    @Query("SELECT * FROM routine_completions WHERE date = :date AND routineId = :routineId LIMIT 1")
    suspend fun getOne(date: String, routineId: String): RoutineCompletionLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RoutineCompletionLocal)

    @Query("DELETE FROM routine_completions WHERE date < :keepFrom")
    suspend fun purgeBefore(keepFrom: String)

    @Query("DELETE FROM routine_completions WHERE routineId = :routineId AND date = :date")
    suspend fun deleteOne(routineId: String, date: String)


}