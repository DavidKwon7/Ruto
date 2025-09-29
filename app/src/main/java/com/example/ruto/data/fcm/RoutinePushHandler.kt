package com.example.ruto.data.fcm

import android.content.Context
import android.os.Build
import com.example.ruto.BuildConfig
import com.example.ruto.data.security.SecureStore
import com.example.ruto.domain.fcm.RegisterFcmModels
import com.example.ruto.util.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FCM 토큰을 안전 저장하고, 서버(Edge Function)로 동기화해 주는 핸들러.
 * - 로그인: Authorization 헤더 사용 (FcmApi 내부 applyAuthHeaders)
 * - 게스트: X-Guest-Id 헤더 사용 (FcmApi 내부 applyAuthHeaders)
 */
@Singleton
class RoutinePushHandler @Inject constructor(
    private val api: FcmApi,
    private val supabase: SupabaseClient,
    private val secure: SecureStore,
    private val logger: AppLogger,
    @ApplicationContext private val appContext: Context
) {
    private val KEY_GUEST_ID = "guest_id"
    private val KEY_FCM_TOKEN = "fcm_token"                 // 마지막 업로드 성공 토큰
    private val KEY_FCM_TOKEN_PENDING = "fcm_token_pending" // 아직 서버 반영 전 토큰(앱 재시작 복구용)

    private val anonKey: String = BuildConfig.SUPABASE_KEY  // SUPABASE_ANON_KEY

    private val scope = CoroutineScope(Dispatchers.IO)

    fun onNewToken(token: String) {
        logger.d("FCM", "onNewToken: $token")

        // 새 토큰을 pending 으로 저장(즉시 종료돼도 복구 가능)
        secure.putString(KEY_FCM_TOKEN_PENDING, token)

        scope.launch { syncToServer(token) }
    }

    /**
     * 앱 부팅 시 불러 pending 토큰이 있으면 서버 반영 시도.
     * (Splash 완료 직후, 또는 App 초기화 시점에서 한 번 호출 권장)
     */
    fun trySyncPendingToken() {
        val pending = secure.getString(KEY_FCM_TOKEN_PENDING)
        if (!pending.isNullOrBlank()) {
            logger.d("PushHandler", "trySyncPendingToken: $pending")
            scope.launch { syncToServer(pending) }
        }
    }

    private suspend fun syncToServer(token: String) {
        val appVersion = readAppVersionOrNull()

        val req = RegisterFcmModels.RegisterFcmRequest(
            token = token,
            platform = "android",
            appVersion = appVersion,
            locale = Locale.getDefault().toLanguageTag()
        )

        var attempt = 0
        val maxAttempts = 3
        val baseDelay = 400L

        while (attempt < maxAttempts) {
            attempt++
            try {
                // 헤더(Authorization or X-Guest-Id)는 FcmApi 내부에서 자동 적용
                api.registerFcmToken(req = req,)
                // 성공: 확정 저장, pending 제거
                secure.putString(KEY_FCM_TOKEN, token)
                secure.clear(KEY_FCM_TOKEN_PENDING)
                logger.d("PushHandler", "FCM token synced")
                return
            } catch (e: Exception) {
                logger.e("PushHandler", "sync fail attempt=$attempt", e)
                if (attempt >= maxAttempts) break
                delay(baseDelay * (1 shl (attempt - 1)))
            }
        }
        // 실패 시 pending 유지 → 다음 기회에 재시도
        logger.e("PushHandler", "FCM token sync permanently failed", null)
    }

    /**
     * 앱 버전을 안전하게 읽어 문자열로 반환 (SDK 버전에 따라 처리)
     */
    private fun readAppVersionOrNull(): String? = try {
        val pm = appContext.packageManager
        val pkg = appContext.packageName
        val pInfo = pm.getPackageInfo(pkg, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pInfo.longVersionCode.toString()
        } else {
            pInfo.versionName
        }
    } catch (e: Exception) {
        null
    }
}
