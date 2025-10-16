package com.example.ruto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ruto.data.local.complete.PendingComplete
import com.example.ruto.data.local.complete.PendingCompleteDao

@Database(
    entities = [PendingComplete::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingCompleteDao(): PendingCompleteDao
}