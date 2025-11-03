package com.example.ruto.data.statistics.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class StatsRange(
    val from: String,        // ISO-8601 local "YYYY-MM-01T00:00:00+09:00" 등
    val toExclusive: String, // 다음달 1일 00:00:00
    val tz: String
)

@Serializable
data class HeatmapDay(
    val date: String,   // "YYYY-MM-DD"
    val count: Int,
    val total: Int,
    val percent: Int
) {
    val safeCount: Int get() = count.coerceAtMost(total.coerceAtLeast(0))
    val safePercent: Int get() =
        if (total > 0) ((safeCount * 100.0) / total).roundToInt().coerceIn(0, 100) else 0
}

@Serializable
data class RoutineDays(
    @SerialName("routine_id") val routineId: String,
    val name: String,
    val days: List<Int> // 0/1
)

@Serializable
data class StatisticsCompletionsResponse(
    val range: StatsRange,
    val heatmap: List<HeatmapDay>,
    val routines: List<RoutineDays>
)