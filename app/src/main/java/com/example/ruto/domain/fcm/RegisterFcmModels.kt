package com.example.ruto.domain.fcm

import kotlinx.serialization.Serializable

class RegisterFcmModels {

    @Serializable
    data class RegisterFcmRequest(
        val token: String,
        val platform: String = "android",
        val appVersion: String? = null,
        val locale: String? = null
    )

    @Serializable
    data class RegisterFcmResponse(
        val ok: Boolean = true
    )
}