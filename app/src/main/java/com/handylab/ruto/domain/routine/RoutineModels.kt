package com.handylab.ruto.domain.routine

import androidx.compose.runtime.Immutable

enum class RoutineCadence { DAILY, WEEKLY, MONTHLY, YEARLY }

@Immutable
data class RoutineCreateRequest(
    val name: String,
    val cadence: RoutineCadence,
    val startDate: String,        // YYYY-MM-DD
    val endDate: String,          // YYYY-MM-DD
    val notifyEnabled: Boolean,
    val notifyTime: String?,      // HH:mm (notifyEnabled=false면 null)
    val timezone: String,         // ex) Asia/Seoul
    val tags: List<String>        // ["건강", "자기개발"]
)

@Immutable
data class RoutineCreateResponse(
    val id: String,
    val createdAt: String
)

/**
 * 루틴 목록/상세 공통으로 쓰는 읽기 모델
 */
@Immutable
data class RoutineRead(
    val id: String,
    val name: String,
    val cadence: RoutineCadence,
    val startDate: String,
    val endDate: String,
    val notifyEnabled: Boolean,
    val notifyTime: String? = null,
    val timezone: String,
    val tags: List<String> = emptyList()
)

@Immutable
data class RoutineListResponse(
    val items: List<RoutineRead>
)

/**
 * 부분 수정 요청.
 * - null 은 “수정 안 함”
 */
@Immutable
data class RoutineUpdateRequest(
    val id: String,
    val name: String? = null,
    val cadence: RoutineCadence? = null,
    val startDate: String? = null, // YYYY-MM-DD
    val endDate: String? = null,     // YYYY-MM-DD
    val notifyEnabled: Boolean? = null,
    val notifyTime: String? = null, // HH:mm (null 설정 시 알림 끔)
    val timezone: String? = null,
    val tags: List<String>? = null
)

@Immutable
data class RoutineUpdateResponse(val ok: Boolean)

@Immutable
data class RoutineDeleteRequest(val id: String)

@Immutable
data class RoutineDeleteResponse(val ok: Boolean)
