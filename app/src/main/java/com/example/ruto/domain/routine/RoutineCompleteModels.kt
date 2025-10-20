package com.example.ruto.domain.routine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompleteItem(
    @SerialName("routine_id") val routineId: String,
    @SerialName("completed_at") val completedAt: String, // ISO-8601 UTC, e.g. 2025-10-15T09:20:00Z
    @SerialName("op_id")val opId: String         // 멱등키(UUID)
)

@Serializable
data class CompleteBatchRequest(
    val items: List<CompleteItem>
)

@Serializable
data class CompleteBatchResponse(
    val ok: Boolean,
    val processed: Int
)
