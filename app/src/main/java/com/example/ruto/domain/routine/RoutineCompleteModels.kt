package com.example.ruto.domain.routine

import kotlinx.serialization.Serializable

@Serializable
data class CompleteItem(
    val routineId: String,
    val completedAt: String, // ISO-8601 UTC, e.g. 2025-10-15T09:20:00Z
    val opId: String         // 멱등키(UUID)
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
