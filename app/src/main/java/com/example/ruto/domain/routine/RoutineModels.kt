package com.example.ruto.domain.routine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class RoutineCadence { DAILY, WEEKLY, MONTHLY, YEARLY }

@Serializable
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

@Serializable
data class RoutineCreateResponse(
    val id: String,
    @SerialName("created_at") val createdAt: String
)

/**
 * 루틴 목록/상세 공통으로 쓰는 읽기 모델
 */
@Serializable
data class RoutineRead(
    val id: String,
    val name: String,
    val cadence: RoutineCadence,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("notify_enabled") val notifyEnabled: Boolean,
    @SerialName("notify_time") val notifyTime: String? = null,
    val timezone: String,
    val tags: List<String> = emptyList()
)

@Serializable
data class RoutineListResponse(
    val items: List<RoutineRead>
)

/**
 * 부분 수정 요청.
 * - null 은 “수정 안 함”
 * - 서버는 snake_case를 받으므로 @SerialName 사용
 */
@Serializable
data class RoutineUpdateRequest(
    val id: String,
    val name: String? = null,
    val cadence: RoutineCadence? = null,
    @SerialName("start_date") val startDate: String? = null, // YYYY-MM-DD
    @SerialName("end_date") val endDate: String? = null,     // YYYY-MM-DD
    @SerialName("notify_enabled") val notifyEnabled: Boolean? = null,
    @SerialName("notify_time") val notifyTime: String? = null, // HH:mm (null 설정 시 알림 끔)
    val timezone: String? = null,
    val tags: List<String>? = null
)

@Serializable
data class RoutineUpdateResponse(val ok: Boolean)

@Serializable
data class RoutineDeleteRequest(val id: String)

@Serializable
data class RoutineDeleteResponse(val ok: Boolean)