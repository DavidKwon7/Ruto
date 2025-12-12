package com.handylab.ruto.data.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import com.handylab.ruto.data.local.RoutineCompletionDao
import com.handylab.ruto.data.local.RoutineCompletionLocal
import com.handylab.ruto.data.local.routine.RoutineDao
import com.handylab.ruto.data.local.routine.toDomain
import com.handylab.ruto.data.security.SecureStore
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.HeatmapDay
import com.handylab.ruto.domain.routine.RoutineDays
import com.handylab.ruto.domain.routine.StatisticsCompletionsResponse
import com.handylab.ruto.domain.routine.StatisticsRange
import com.handylab.ruto.util.ensureGuestId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
class LiveMonthlyStatsCalculator @Inject constructor(
    private val routineDao: RoutineDao,
    private val completionDao: RoutineCompletionDao,
    private val supabase: SupabaseClient,
    private val secure: SecureStore,
) {
    private val dateFmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun ownerKey(): String {
        val uid = supabase.auth.currentSessionOrNull()?.user?.id
        return if (uid != null) "user:$uid" else "guest:${ensureGuestId(secure)}"
    }

    /** tz 기준 month("YYYY-MM")의 [from, toExclusive) 로컬 범위를 계산 */
    private fun monthRange(tz: ZoneId, month: String): Pair<LocalDate, LocalDate> {
        val start = LocalDate.parse("$month-01")
        val endExclusive = start.plusMonths(1)
        return start to endExclusive
    }

    /** 서버와 동일한 “해당 로컬 날짜에 활성인 루틴 수(total)” 규칙 */
    private fun isActiveOn(date: LocalDate, r: RoutineRead): Boolean {
        val s = LocalDate.parse(r.startDate)
        val e = r.endDate.takeIf { it.isNotBlank() }?.let(LocalDate::parse)
        if (date.isBefore(s)) return false
        if (e != null && date.isAfter(e)) return false
        // cadence 주기 필터는 필요 시 확장
        return true
    }

    /** 월간 통계 로컬 투영 스트림
     * - tz: 예) "Asia/Seoul"
    + - month: "YYYY-MM"
     * */
    fun observeMonthly(tz: String, month: String): Flow<StatisticsCompletionsResponse> {
        val zone = ZoneId.of(tz)
        val (from, toEx) = monthRange(zone, month)
        val ok = ownerKey()
        val days = ChronoUnit.DAYS.between(from, toEx).toInt()

        val routinesFlow = routineDao.observeAll(ok)
            .map { list -> list.map { it.toDomain() } }

        val completesFlow = completionDao.observeRange(
            ok,
            from.format(dateFmt),
            toEx.minusDays(1).format(dateFmt)
        )

        return combine(routinesFlow, completesFlow) { routines, completes ->
            computeResponse(zone, month, from, toEx, routines, completes, days)
        }
    }

    private fun computeResponse(
        zone: ZoneId,
        month: String,
        from: LocalDate,
        toEx: LocalDate,
        routines: List<RoutineRead>,
        completes: List<RoutineCompletionLocal>,
        days: Int
    ): StatisticsCompletionsResponse {
        // 날짜 인덱스
        val idxByDate = (0 until days).associate { i ->
            val d = from.plusDays(i.toLong()).format(dateFmt)
            d to i
        }

        // 날짜별 Distinct 완료 루틴 집합(분자)
        val perDateDistinct = Array(days) { mutableSetOf<String>() }
        completes.forEach { row ->
            val i = idxByDate[row.date] ?: return@forEach
            if (row.completed) perDateDistinct[i] += row.routineId
        }

        // 날짜별 total(분모): 해당 날짜에 활성 루틴 수
        val totals = IntArray(days) { i ->
            val d = from.plusDays(i.toLong())
            routines.count { isActiveOn(d, it) }
        }
        // heatmap 조립 (서버와 동일: percent = min(100, round(count/total*100)))
        val heatmap = (0 until days).map { i ->
            val date = from.plusDays(i.toLong()).format(dateFmt)
            val count = perDateDistinct[i].size
            val total = totals[i]
            val percent = if (total > 0) {
                min(100, ((count * 100.0) / total).roundToInt())
            } else 0
            HeatmapDay(date = date, count = count, total = total, percent = percent)
        }

        // 루틴별 0/1 days 배열
        val routineIdToDays = routines.associate { it.id to IntArray(days) { 0 } }.toMutableMap()
        completes.forEach { row ->
            val i = idxByDate[row.date] ?: return@forEach
            if (row.completed) routineIdToDays[row.routineId]?.set(i, 1)
        }

        val routineRows = routines.map { r ->
            RoutineDays(
                routineId = r.id,
                name = r.name,
                days = (routineIdToDays[r.id] ?: IntArray(days) { 0 }).toList()
            )
        }

        // range 값 (서버 응답과 동일한 형태)
        val fromIsoLocal = from.atStartOfDay(zone).toOffsetDateTime().toString()
        val toIsoLocal = toEx.atStartOfDay(zone).toOffsetDateTime().toString()
        val range = StatisticsRange(
            from = fromIsoLocal,
            toExclusive = toIsoLocal,
            tz = zone.id
        )
        return StatisticsCompletionsResponse(
            range = range,
            heatmap = heatmap,
            routines = routineRows
        )
    }

}