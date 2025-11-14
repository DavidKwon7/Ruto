package com.handylab.ruto.data.local.complete

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_completes")
data class PendingComplete(
    @PrimaryKey val opId: String,        // 멱등키
    val routineId: String,
    val completedAtIso: String           // ISO-8601(UTC)
)
