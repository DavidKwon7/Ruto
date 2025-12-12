package com.handylab.ruto.data.routine

import android.os.Build
import androidx.annotation.RequiresApi
import com.handylab.ruto.data.local.RoutineCompletionDao
import com.handylab.ruto.data.local.RoutineCompletionLocal
import com.handylab.ruto.data.local.routine.RoutineDao
import com.handylab.ruto.data.local.routine.toDomain
import com.handylab.ruto.data.local.routine.toEntity
import com.handylab.ruto.data.security.SecureStore
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineCreateRequest
import com.handylab.ruto.domain.routine.RoutineCreateResponse
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.RoutineRepository
import com.handylab.ruto.domain.routine.RoutineTag
import com.handylab.ruto.domain.routine.RoutineUpdateRequest
import com.handylab.ruto.domain.routine.towrite
import com.handylab.ruto.util.ensureGuestId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@RequiresApi(Build.VERSION_CODES.O)
class RoutineRepositoryImpl @Inject constructor(
    private val api: RoutineApi,
    private val supabase: SupabaseClient,
    private val secure: SecureStore,
    private val completionDao: RoutineCompletionDao,
    private val routineDao: RoutineDao,
) : RoutineRepository {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE   // YYYY-MM-DD

    private fun ownerKey(): String {
        val user = supabase.auth.currentSessionOrNull()?.user?.id
        return if (user != null) "user:$user" else "guest:${ensureGuestId(secure)}"
    }

    override fun observeRoutineList(): Flow<List<RoutineRead>> =
        routineDao.observeAll(ownerKey())
            .map { list -> list.map { it.toDomain() } }

    override fun observeRoutine(id: String): Flow<RoutineRead?> =
        routineDao.observeOne(ownerKey(), id)
            .map { it?.toDomain() }

    override fun observeTodayCompletionIds(): Flow<Set<String>> =
        completionDao.observeByDate(ownerKey(), LocalDate.now().format(dateFmt))
            .map { rows -> rows.filter { it.completed }.map { it.routineId }.toSet() }

    override suspend fun refreshRoutines() {
        val ok = ownerKey()
        val remote = api.getRoutineList().items
        routineDao.upsertAll(remote.map { it.toEntity(ok) })
    }

    override suspend fun fetchRoutine(id: String): Result<RoutineRead> = runCatching {
        api.getRoutine(id)
    }

    override suspend fun registerRoutine(
        name: String,
        cadence: RoutineCadence,
        startDate: LocalDate,
        endDate: LocalDate,
        notifyEnabled: Boolean,
        notifyTime: String?,
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
            notifyTime = notifyTime,
            timezone = TimeZone.getDefault().id,
            tags = tagStrings,
        )
        val resp = api.createRoutine(req)
        val created = api.getRoutine(resp.id)
        routineDao.upsert(created.toEntity(ownerKey()))
        resp
    }

    override suspend fun updateRoutine(req: RoutineUpdateRequest): Result<Boolean> = runCatching {
        val ok = ownerKey()

        val before = routineDao.getOne(ok, req.id)?.toDomain()
            ?: api.getRoutine(req.id)

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
            routineDao.upsert(before.toEntity(ok))
            return@runCatching false
        }

        val latest = api.getRoutine(req.id)
        routineDao.upsert(latest.toEntity(ok))
        true
    }

    override suspend fun deleteRoutine(id: String): Result<Boolean> = runCatching {
        val ok = ownerKey()
        val snapshot = routineDao.getOne(ok, id)
        routineDao.deleteById(ok, id)

        val okRemote = api.deleteRoutine(id).ok
        if (!okRemote) {
            if (snapshot != null) routineDao.upsert(snapshot)
            return@runCatching false
        }
        true
    }

    override suspend fun setCompletionLocal(routineId: String, completed: Boolean) {
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
