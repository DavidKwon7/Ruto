package com.handylab.ruto

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import com.handylab.ruto.workManager.AppWorkerFactory
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp() : Application(), Configuration.Provider {

    // @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var workerFactory: AppWorkerFactory

    override fun onCreate() {
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        ensureRoutineChannel(this)

        val cfg = workManagerConfiguration
        androidx.work.WorkManager.initialize(this, cfg)
    }


    override val workManagerConfiguration: Configuration get() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}

fun ensureRoutineChannel(ctx: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "routine", "Routine", NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

    }
}