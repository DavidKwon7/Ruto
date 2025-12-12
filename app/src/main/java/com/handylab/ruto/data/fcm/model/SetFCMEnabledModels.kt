package com.handylab.ruto.data.fcm.model

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
