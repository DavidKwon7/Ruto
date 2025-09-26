package com.example.ruto.domain.routine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class RoutineCadence { DAILY, WEEKLY, MONTHLY, YEARLY }

@Serializable
data class RoutineCreateRequest(
    val name: String,
    val cadence: RoutineCadence,
    @SerialName("start_date") val startDate: String,           // YYYY-MM-DD (ISO-8601
    @SerialName("end_date") val endDate: String,             // YYYY-MM-DD
    @SerialName("notify_enabled") val notifyEnabled: Boolean,
    @SerialName("notify_time") val notifyTime: String?,         // HH:mm (로컬시간), notifyEnabled=false면 null
    val timezone: String,            // 예: Asia/Seoul
    val tags: List<String>,
    val fcmToken: String? = null,     // 알림 ON이면 서버로 전달
    val guestId: String? = null   // 게스트 식별자(게스트 모드일 때만)
)

@Serializable
data class RoutineCreateResponse(
    val id: String,
    @SerialName("created_at") val createdAt: String
)