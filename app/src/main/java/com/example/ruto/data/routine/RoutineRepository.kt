package com.example.ruto.data.routine

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ruto.data.local.RoutineCompletionDao
import com.example.ruto.data.local.RoutineCompletionLocal
import com.example.ruto.data.notification.FcmTokenProvider
import com.example.ruto.data.security.SecureStore
import com.example.ruto.domain.routine.RoutineCadence
import com.example.ruto.domain.routine.RoutineCreateRequest
import com.example.ruto.domain.routine.RoutineCreateResponse
import com.example.ruto.domain.routine.RoutineRead
import com.example.ruto.domain.routine.RoutineTag
import com.example.ruto.domain.routine.RoutineUpdateRequest
import com.example.ruto.domain.routine.towrite
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class RoutineRepository @Inject constructor(
    private val api: RoutineApi,
    private val fcm: FcmTokenProvider,
    private val supabase: SupabaseClient,
    private val secure: SecureStore,
    private val completionDao: RoutineCompletionDao
) {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE   // YYYY-MM-DD
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    suspend fun registerRoutine(
        name: String,
        cadence: RoutineCadence,
        startDate: LocalDate,
        endDate: LocalDate,
        notifyEnabled: Boolean,
        notifyTime: LocalTime?,
        tags: List<RoutineTag>
    ): Result<RoutineCreateResponse> = runCatching {

        require(name.isNotBlank()) { "루틴 명칭을 입력하세요." }
        require(!startDate.isAfter(endDate)) { "실행 기간이 올바르지 않습니다." }
        if (notifyEnabled) require(notifyTime != null) { "알림 시간을 선택하세요." }

        val tagStrings = tags.map { it.towrite() }.filter { it.isNotBlank() }.distinct()

        val req = RoutineCreateRequest(
            name = name.trim(),
            cadence = cadence,
            startDate = startDate.format(dateFmt),
            endDate = endDate.format(dateFmt),
            notifyEnabled = notifyEnabled,
            notifyTime = notifyTime?.format(timeFmt),
            timezone = TimeZone.getDefault().id,       // ex) Asia/Seoul
            tags = tagStrings,
        )
        api.createRoutine(req)
    }

    suspend fun getRoutineList(): Result<List<RoutineRead>> = runCatching {
        api.getRoutineList().items
    }

    suspend fun getRoutine(id: String): Result<RoutineRead> = runCatching {
        api.getRoutine(id)
    }

    suspend fun updateRoutine(req: RoutineUpdateRequest): Result<Boolean> = runCatching {
        api.updateRoutine(req).ok
    }

    suspend fun deleteRoutine(id: String): Result<Boolean> = runCatching {
        api.deleteRoutine(id).ok
    }

    /** 오늘(로컬)의 완료 상태 스트림 */
    fun observeTodayCompletions(): Flow<List<RoutineCompletionLocal>> {
        val today = LocalDate.now().format(dateFmt)
        return completionDao.observeByDate(today)
    }

    /** 오늘(로컬) 완료 상태를 로컬 DB에 반영 (true=저장, false=삭제) */
    suspend fun setCompletionLocal(routineId: String, completed: Boolean) {
        val today = LocalDate.now().format(dateFmt)
        if (completed) {
            val key = "$routineId#$today"
            completionDao.upsert(
                RoutineCompletionLocal(
                    key = key,
                    routineId = routineId,
                    date = today,
                    completed = true,
                    synced = false,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            completionDao.deleteOne(routineId = routineId, date = today)
        }
    }
}
