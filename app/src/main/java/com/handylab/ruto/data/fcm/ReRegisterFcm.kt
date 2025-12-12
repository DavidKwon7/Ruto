package com.handylab.ruto.data.fcm

import com.handylab.ruto.BuildConfig
import com.handylab.ruto.data.fcm.model.RegisterFcmModels
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.util.Locale

suspend fun reRegisterFcm(fcmApi: FcmApi) {
    val token = FirebaseMessaging.getInstance().token.await()
    fcmApi.registerFcmToken(
        req = RegisterFcmModels.RegisterFcmRequest(
            token = token,
            platform = "android",
            appVersion = BuildConfig.VERSION_NAME,
            locale = Locale.getDefault().toLanguageTag()
        )
    )
}