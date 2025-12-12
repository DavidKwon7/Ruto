package com.handylab.ruto.data.routine

import com.handylab.ruto.domain.routine.CompleteBatchRequest
import com.handylab.ruto.domain.routine.CompleteBatchResponse
import com.handylab.ruto.domain.routine.CompleteItem
import com.handylab.ruto.domain.routine.RoutineCadence
import com.handylab.ruto.domain.routine.RoutineCreateRequest
import com.handylab.ruto.domain.routine.RoutineCreateResponse
import com.handylab.ruto.domain.routine.RoutineDeleteResponse
import com.handylab.ruto.domain.routine.RoutineListResponse
import com.handylab.ruto.domain.routine.RoutineRead
import com.handylab.ruto.domain.routine.RoutineUpdateRequest
import com.handylab.ruto.domain.routine.RoutineUpdateResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoutineCreateRequestDto(
    val name: String,
    val cadence: RoutineCadence,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("notify_enabled") val notifyEnabled: Boolean,
    @SerialName("notify_time") val notifyTime: String?,
    val timezone: String,
    val tags: List<String>
) {
    companion object {
        fun fromDomain(request: RoutineCreateRequest) = RoutineCreateRequestDto(
            name = request.name,
            cadence = request.cadence,
            startDate = request.startDate,
            endDate = request.endDate,
            notifyEnabled = request.notifyEnabled,
            notifyTime = request.notifyTime,
            timezone = request.timezone,
            tags = request.tags,
        )
    }
}

@Serializable
data class RoutineCreateResponseDto(
    val id: String,
    @SerialName("created_at") val createdAt: String
) {
    fun toDomain() = RoutineCreateResponse(
        id = id,
        createdAt = createdAt,
    )
}

@Serializable
data class RoutineReadDto(
    val id: String,
    val name: String,
    val cadence: RoutineCadence,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("notify_enabled") val notifyEnabled: Boolean,
    @SerialName("notify_time") val notifyTime: String? = null,
    val timezone: String,
    val tags: List<String> = emptyList()
) {
    fun toDomain() = RoutineRead(
        id = id,
        name = name,
        cadence = cadence,
        startDate = startDate,
        endDate = endDate,
        notifyEnabled = notifyEnabled,
        notifyTime = notifyTime,
        timezone = timezone,
        tags = tags,
    )
}

@Serializable
data class RoutineListResponseDto(
    val items: List<RoutineReadDto>
) {
    fun toDomain() = RoutineListResponse(items = items.map { it.toDomain() })
}

@Serializable
data class RoutineUpdateRequestDto(
    val id: String,
    val name: String? = null,
    val cadence: RoutineCadence? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("notify_enabled") val notifyEnabled: Boolean? = null,
    @SerialName("notify_time") val notifyTime: String? = null,
    val timezone: String? = null,
    val tags: List<String>? = null
) {
    companion object {
        fun fromDomain(request: RoutineUpdateRequest) = RoutineUpdateRequestDto(
            id = request.id,
            name = request.name,
            cadence = request.cadence,
            startDate = request.startDate,
            endDate = request.endDate,
            notifyEnabled = request.notifyEnabled,
            notifyTime = request.notifyTime,
            timezone = request.timezone,
            tags = request.tags,
        )
    }
}

@Serializable
data class RoutineUpdateResponseDto(val ok: Boolean) {
    fun toDomain() = RoutineUpdateResponse(ok = ok)
}

@Serializable
data class RoutineDeleteRequestDto(val id: String)

@Serializable
data class RoutineDeleteResponseDto(val ok: Boolean) {
    fun toDomain() = RoutineDeleteResponse(ok = ok)
}

@Serializable
data class CompleteItemDto(
    @SerialName("routine_id") val routineId: String,
    @SerialName("completed_at") val completedAt: String,
    @SerialName("op_id") val opId: String
) {
    companion object {
        fun fromDomain(item: CompleteItem) = CompleteItemDto(
            routineId = item.routineId,
            completedAt = item.completedAt,
            opId = item.opId,
        )
    }
}

@Serializable
data class CompleteBatchRequestDto(
    val items: List<CompleteItemDto>
) {
    companion object {
        fun fromDomain(request: CompleteBatchRequest) = CompleteBatchRequestDto(
            items = request.items.map { CompleteItemDto.fromDomain(it) },
        )
    }
}

@Serializable
data class CompleteBatchResponseDto(
    val ok: Boolean,
    val processed: Int
) {
    fun toDomain() = CompleteBatchResponse(
        ok = ok,
        processed = processed,
    )
}
