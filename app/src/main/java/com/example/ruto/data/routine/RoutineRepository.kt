package com.example.ruto.data.routine

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ruto.data.notification.FcmTokenProvider
import com.example.ruto.data.security.SecureStore
import com.example.ruto.domain.routine.RoutineCadence
import com.example.ruto.domain.routine.RoutineCreateRequest
import com.example.ruto.domain.routine.RoutineCreateResponse
import com.example.ruto.domain.routine.RoutineTag
import com.example.ruto.domain.routine.towrite
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
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
    private val secure: SecureStore
) {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE   // YYYY-MM-DD
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val KEY_GUEST_ID = "guest_id"

    private fun ensureGuestId(): String {
        var id = secure.getString(KEY_GUEST_ID)
        if (id.isNullOrBlank()) {
            id = java.util.UUID.randomUUID().toString()
            secure.putString(KEY_GUEST_ID, id)
        }
        return id
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
        // 1) 검증
        require(name.isNotBlank()) { "루틴 명칭을 입력하세요." }
        require(!startDate.isAfter(endDate)) { "실행 기간이 올바르지 않습니다." }
        if (notifyEnabled) require(notifyTime != null) { "알림 시간을 선택하세요." }


        // 2) FCM 토큰 (알림 ON일 때만)
        val token = if (notifyEnabled) fcm.getToken(forceRefresh = false) else null

        val tagStrings = tags.map { it.towrite() }.filter { it.isNotBlank() }.distinct()

        // 3) 요청 본문 생성
        val req = RoutineCreateRequest(
            name = name.trim(),
            cadence = cadence,
            startDate = startDate.format(dateFmt),
            endDate = endDate.format(dateFmt),
            notifyEnabled = notifyEnabled,
            notifyTime = notifyTime?.format(timeFmt),
            timezone = TimeZone.getDefault().id,       // ex) Asia/Seoul
            tags = tagStrings,
            fcmToken = token
        )
        val guestId = supabase.auth.currentSessionOrNull()?.let { null } ?: ensureGuestId()

        // 4) 전송
        //api.createRoutine(req, guestId)
        api.createRoutine(req)
    }
}
