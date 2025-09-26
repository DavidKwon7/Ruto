package com.example.ruto

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.example.ruto.data.fcm.RoutinePushHandler
import com.example.ruto.util.AppLogger
import com.example.ruto.util.NotificationUtil
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import java.util.logging.Handler
import java.util.logging.Logger
import javax.inject.Inject

@AndroidEntryPoint
class AppFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var logger: AppLogger
    @Inject lateinit var routinePushHandler: RoutinePushHandler

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        logger.d("FCM", "new token: $token")
        // 서버에 토큰 동기화(로그인 사용자거나 게스트 식별자와 함께)
        // routinePushHandler.onNewFcmToken(token)
        routinePushHandler.onNewToken(token)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        logger.d("FCM", "msg: ${message.data}")

        NotificationUtil.show(this, title = message.notification?.title ?: "알림",
            body = message.notification?.body ?: message.data["body"].orEmpty())
    }
}