package com.handylab.ruto.data.local.statistics

import androidx.room.Entity

@Entity(tableName = "statistics_cache", primaryKeys = ["key"])
data class StatisticsEntity(
    val key: String,              // "$month|$tz|$scope"
    val payloadJson: String,
    val updatedAt: Long
)
