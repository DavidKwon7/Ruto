package com.example.ruto.data.local.statistics

import androidx.room.Entity

@Entity(tableName = "statistics_cache", primaryKeys = ["key"])
data class StatisticsLocal(
    val key: String,              // "$month|$tz|$scope"
    val payloadJson: String,
    val updatedAt: Long
)
