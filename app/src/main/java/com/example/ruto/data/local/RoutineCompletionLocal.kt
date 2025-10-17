package com.example.ruto.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 날짜별 완료 여부 로컬 캐시.
 * - routineId + date(YYYY-MM-DD) 를 유니크로 보장
 * - key = "$routineId#$date" 를 PK로 사용 (간단하고 빠름)
 */
@Entity(
    tableName = "routine_completions",
    indices = [Index(value = ["routineId", "date"], unique = true)]
)
data class RoutineCompletionLocal(
    @PrimaryKey val key: String,      // "$routineId#$date"
    val routineId: String,
    val date: String,                 // YYYY-MM-DD (로컬 타임존 기준)
    val completed: Boolean,           // true = 완료
    val synced: Boolean = false,      // 서버 반영 여부(옵션)
    val updatedAt: Long = System.currentTimeMillis()
)
