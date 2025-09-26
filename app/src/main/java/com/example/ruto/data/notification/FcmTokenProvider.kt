package com.example.ruto.data.notification

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// TODO FCM 기능 추가
@Singleton
class FcmTokenProvider @Inject constructor(
    private val messaging: FirebaseMessaging,
) {
    suspend fun getToken(forceRefresh: Boolean = false): String {
        if (forceRefresh) messaging.deleteToken().await()
        return messaging.token.await()
    }
}