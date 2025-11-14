package com.handylab.ruto.domain.fcm

import kotlinx.serialization.Serializable

class RegisterFcmModels {

    @Serializable
    data class RegisterFcmRequest(
        val token: String,
        val platform: String = "android",
        val appVersion: String? = null,
        val locale: String? = null,
        val deviceId: String? = null,   // ANDROID_ID
        val installationId: String? = null  // Firebase Installations ID
    )

    @Serializable
    data class RegisterFcmResponse(
        val ok: Boolean
    )
}