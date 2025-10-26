package com.example.ruto.data.local.routine

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("""
        SELECT * FROM routines_cached
        WHERE ownerKey = :ownerKey
        ORDER BY updatedAt DESC
    """)
    fun observeAll(ownerKey: String): Flow<List<RoutineEntity>>


    @Query("""
        SELECT * FROM routines_cached
        WHERE ownerKey = :ownerKey AND id = :id
        LIMIT 1
    """)
    fun observeOne(ownerKey: String, id: String): Flow<RoutineEntity?>

    @Query("""
        SELECT * FROM routines_cached
        WHERE ownerKey = :ownerKey AND id = :id
        LIMIT 1
    """)
    suspend fun getOne(ownerKey: String, id: String): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<RoutineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RoutineEntity)

    @Query("""
        DELETE FROM routines_cached
        WHERE ownerKey = :ownerKey AND id = :id
    """)
    suspend fun deleteById(ownerKey: String, id: String)

}