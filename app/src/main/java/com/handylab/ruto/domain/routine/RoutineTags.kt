package com.handylab.ruto.domain.routine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FixedTag {
    @SerialName("health") HEALTH,
    @SerialName("self_improvement") SELF_IMPROVEMENT,
    @SerialName("household") HOUSEHOLD,
    @SerialName("finance") FINANCE,
    @SerialName("study") STUDY,
    @SerialName("other") OTHER
}

@Serializable
sealed class RoutineTag {
    @Serializable @SerialName("fixed")
    data class Fixed(val code: FixedTag): RoutineTag()
    @Serializable @SerialName("custom")
    data class Custom(val label: String): RoutineTag()
}


fun RoutineTag.towrite(): String = when (this) {
    is RoutineTag.Fixed -> when (code) {
        FixedTag.HEALTH -> "health"
        FixedTag.SELF_IMPROVEMENT -> "self_improvement"
        FixedTag.HOUSEHOLD -> "household"
        FixedTag.FINANCE -> "finance"
        FixedTag.STUDY -> "study"
        FixedTag.OTHER -> "other"
    }
    is RoutineTag.Custom -> label.trim()
}