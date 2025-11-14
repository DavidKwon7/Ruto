package com.handylab.ruto.domain.fcm

import kotlinx.serialization.Serializable

class SetFCMEnabledModels {
    @Serializable
    data class Request(
        val token: String,
        val enabled: Boolean
    )

    @Serializable
    data class Response(
        val ok: Boolean
    )
}