package com.example.ruto.domain.routine

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class StatisticsCompletionsResponse(
    val range: StatisticsRange,
    val heatmap: List<HeatmapDay>,
    val routines: List<RoutineDays>
)

@Serializable
data class StatisticsRange(
    val from: String,          // 2025-10-01T00:00:00+09:00
    @SerialName("toExclusive") val toExclusive: String,
    val tz: String
)

@Serializable
data class HeatmapDay(
    val date: String,          // YYYY-MM-DD (tz 기준)
    val count: Int,            // 완료 루틴 수(분자)
    val total: Int,            // 그날 대상 루틴 전체(분모)
    val percent: Int           // 0..100
)

@Serializable
data class RoutineDays(
    @SerialName("routine_id") val routineId: String,
    val name: String = "",
    val days: List<Int>        // 해당 달 일수길이의 0/1 배열
)
