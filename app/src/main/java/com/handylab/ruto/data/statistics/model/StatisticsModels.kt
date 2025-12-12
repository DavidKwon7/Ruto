package com.handylab.ruto.data.statistics.model

import androidx.compose.runtime.Immutable
import com.handylab.ruto.domain.routine.HeatmapDay
import com.handylab.ruto.domain.routine.RoutineDays
import com.handylab.ruto.domain.routine.StatisticsCompletionsResponse
import com.handylab.ruto.domain.routine.StatisticsRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Immutable
@Serializable
data class StatisticsCompletionsResponseDto(
    val range: StatisticsRangeDto,
    val heatmap: List<HeatmapDayDto>,
    val routines: List<RoutineDaysDto>,
) {
    fun toDomain() = StatisticsCompletionsResponse(
        range = range.toDomain(),
        heatmap = heatmap.map { it.toDomain() },
        routines = routines.map { it.toDomain() },
    )
}

fun StatisticsCompletionsResponse.toDto() = StatisticsCompletionsResponseDto(
    range = StatisticsRangeDto(
        from = range.from,
        toExclusive = range.toExclusive,
        tz = range.tz,
    ),
    heatmap = heatmap.map {
        HeatmapDayDto(
            date = it.date,
            count = it.count,
            total = it.total,
            percent = it.percent,
        )
    },
    routines = routines.map {
        RoutineDaysDto(
            routineId = it.routineId,
            name = it.name,
            days = it.days,
        )
    },
)


@Immutable
@Serializable
data class RoutineDaysDto(
    @SerialName("routine_id") val routineId: String,
    val name: String,
    val days: List<Int> // 0/1
) {
    fun toDomain() = RoutineDays(
        routineId = routineId,
        name = name,
        days = days,
    )
}

@Immutable
@Serializable
data class StatisticsRangeDto(
    val from: String,        // ISO-8601 local "YYYY-MM-01T00:00:00+09:00" 등
    val toExclusive: String, // 다음달 1일 00:00:00
    val tz: String,
) {
    fun toDomain() = StatisticsRange(
        from = from,
        toExclusive = toExclusive,
        tz = tz,
    )
}

@Immutable
@Serializable
data class HeatmapDayDto(
    val date: String,   // "YYYY-MM-DD"
    val count: Int,
    val total: Int,
    val percent: Int,
) {
    val safeCount: Int get() = count.coerceAtMost(total.coerceAtLeast(0))
    val safePercent: Int get() =
        if (total > 0) ((safeCount * 100.0) / total).roundToInt().coerceIn(0, 100) else 0

    fun toDomain() = HeatmapDay(
        date = date,
        count = count,
        total = total,
        percent = percent,
    )
}