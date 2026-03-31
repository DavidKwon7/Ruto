package com.handylab.ruto.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 새 테이블 생성 (ownerKey 포함)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS routine_completions (
              key TEXT NOT NULL PRIMARY KEY,
              ownerKey TEXT NOT NULL DEFAULT '',
              routineId TEXT NOT NULL,
              date TEXT NOT NULL,
              completed INTEGER NOT NULL,
              synced INTEGER NOT NULL DEFAULT 0,
              updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        // 유니크 인덱스 (ownerKey + routineId + date)
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_routine_completions_ownerKey_routineId_date
            ON routine_completions(ownerKey, routineId, date)
            """.trimIndent()
        )
    }
}
