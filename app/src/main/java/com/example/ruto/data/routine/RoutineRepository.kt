package com.example.ruto.data.routine

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ruto.data.local.RoutineCompletionDao
import com.example.ruto.data.local.RoutineCompletionLocal
import com.example.ruto.data.local.routine.RoutineDao
import com.example.ruto.data.local.routine.toDomain
import com.example.ruto.data.local.routine.toEntity
import com.example.ruto.data.notification.FcmTokenProvider
import com.example.ruto.data.security.SecureStore
import com.example.ruto.domain.routine.RoutineCadence
import com.example.ruto.domain.routine.RoutineCreateRequest
import com.example.ruto.domain.routine.RoutineCreateResponse
import com.example.ruto.domain.routine.RoutineRead
import com.example.ruto.domain.routine.RoutineTag
import com.example.ruto.domain.routine.RoutineUpdateRequest
import com.example.ruto.domain.routine.towrite
import com.example.ruto.util.ensureGuestId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
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
    private val completionDao: RoutineCompletionDao,
    private val routineDao: RoutineDao,
) {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE   // YYYY-MM-DD
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    /** user:<uid> | guest:<gid> */
    private fun ownerKey(): String {
        val user = supabase.auth.currentSessionOrNull()?.user?.id
        return if (user != null) "user:$user" else "guest:${ensureGuestId(secure)}"
    }

    /** 목록 스트림 (계정별) */
    fun observeRoutineList(): Flow<List<RoutineRead>> =
        routineDao.observeAll(ownerKey())
            .map { list -> list.map { it.toDomain() } }

    /** 단건 스트림 (계정별) */
    fun observeRoutine(id: String): Flow<RoutineRead?> =
        routineDao.observeOne(ownerKey(), id)
            .map { it?.toDomain() }

    /** 서버에서 최신 목록을 끌어와 DB에 반영(동기화) */
    suspend fun syncRoutineList() {
        val ok = ownerKey()
        val remote = api.getRoutineList().items
        routineDao.upsertAll(remote.map { it.toEntity(ok) })
    }

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
        // api.createRoutine(req)
        val resp = api.createRoutine(req)
        val created = api.getRoutine(resp.id)
        routineDao.upsert(created.toEntity(ownerKey()))
        resp
    }

    suspend fun getRoutineList(): Result<List<RoutineRead>> = runCatching {
        api.getRoutineList().items
    }

    suspend fun getRoutine(id: String): Result<RoutineRead> = runCatching {
        api.getRoutine(id)
    }

    suspend fun updateRoutine(req: RoutineUpdateRequest): Result<Boolean> = runCatching {
        val ok = ownerKey()

        val before = routineDao.getOne(ok, req.id)?.toDomain()
            ?: api.getRoutine(req.id)

        // 낙관적 캐시
        val optimistic = before.copy(
            name = req.name ?: before.name,
            cadence = req.cadence ?: before.cadence,
            startDate = req.startDate ?: before.startDate,
            endDate   = req.endDate   ?: before.endDate,
            notifyEnabled = req.notifyEnabled ?: before.notifyEnabled,
            notifyTime    = req.notifyTime    ?: before.notifyTime,
            timezone      = req.timezone      ?: before.timezone,
            tags          = req.tags          ?: before.tags
        )
        routineDao.upsert(optimistic.toEntity(ok))

        val okRemote = api.updateRoutine(req).ok
        if (!okRemote) {
            // 롤백
            routineDao.upsert(before.toEntity(ok))
            return@runCatching false
        }

        // 최신 단건 재주입
        val latest = api.getRoutine(req.id)
        routineDao.upsert(latest.toEntity(ok))
        true
    }

    suspend fun deleteRoutine(id: String): Result<Boolean> = runCatching {
        val ok = ownerKey()
        val snapshot = routineDao.getOne(ok, id)
        routineDao.deleteById(ok, id)

        val okRemote = api.deleteRoutine(id).ok
        if (!okRemote) {
            // 실패시 복구
            if (snapshot != null) routineDao.upsert(snapshot)
            return@runCatching false
        }
        true
    }

    suspend fun refreshFromServer() = syncRoutineList()

    /** 오늘(로컬)의 완료 목록 스트림 */
    fun observeTodayCompletions(): Flow<List<RoutineCompletionLocal>> {
        val today = LocalDate.now().format(dateFmt)
        return completionDao.observeByDate(ownerKey(), today)
    }

    /** 오늘 완료 토글 로컬 저장 (계정별) */
    suspend fun setCompletionLocal(routineId: String, completed: Boolean) {
        val today = LocalDate.now().format(dateFmt)
        val ok = ownerKey()
        val entity = RoutineCompletionLocal(
            key = "$ok#$routineId#$today",
            ownerKey = ok,
            routineId = routineId,
            date = today,
            completed = completed,
            synced = false,
            updatedAt = System.currentTimeMillis()
        )
        completionDao.upsert(entity)
    }
}
