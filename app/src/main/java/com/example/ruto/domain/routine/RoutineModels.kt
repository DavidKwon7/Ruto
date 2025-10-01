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