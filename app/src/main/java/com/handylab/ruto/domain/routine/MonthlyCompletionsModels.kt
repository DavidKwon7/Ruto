package com.handylab.ruto.domain.routine

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

@Immutable
data class StatisticsCompletionsResponse(
    val range: StatisticsRange,
    val heatmap: List<HeatmapDay>,
    val routines: List<RoutineDays>
)

@Immutable
data class StatisticsRange(
    val from: String,          // 2025-10-01T00:00:00+09:00
    val toExclusive: String,
    val tz: String
)

@Immutable
data class HeatmapDay(
    val date: String,          // YYYY-MM-DD (tz 기준)
    val count: Int,            // 완료 루틴 수(분자)
    val total: Int,            // 그날 대상 루틴 전체(분모)
    val percent: Int           // 0..100
) {
    val safeCount: Int get() = count.coerceAtMost(total.coerceAtLeast(0))
    val safePercent: Int get() =
        if (total > 0) ((safeCount * 100.0) / total).roundToInt().coerceIn(0, 100) else 0
}

@Immutable
data class RoutineDays(
    val routineId: String,
    val name: String = "",
    val days: List<Int>        // 해당 달 일수길이의 0/1 배열
)
