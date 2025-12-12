package com.handylab.ruto.data.fcm

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.handylab.ruto.data.security.SecureStore
import com.handylab.ruto.data.fcm.model.RegisterFcmModels
import com.handylab.ruto.util.AppLogger
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
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
    @param:ApplicationContext private val appContext: Context
) {
    private val KEY_FCM_TOKEN = "fcm_token"                 // 마지막 업로드 성공 토큰
    private val KEY_FCM_TOKEN_PENDING = "fcm_token_pending" // 아직 서버 반영 전 토큰(앱 재시작 복구용)
    private val KEY_PUSH_ENABLED = "push_enabled"

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Android_ID (device_id)
     */
    @SuppressLint("HardwareIds")
    private fun readAndroidId(): String? = try {
        Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
    } catch (e: Exception) { null }

    /**
     * Firebase Installations ID (installation_id)
     */
    private suspend fun readInstallationsId(): String? = try {
        suspendCancellableCoroutine { continuation ->
            FirebaseInstallations.getInstance().id
                .addOnSuccessListener { continuation.resume(it, onCancellation = null) }
                .addOnFailureListener { continuation.resume(null, onCancellation = null) }
        }
    }catch (e: Exception) { null }

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

    fun getEnabledLocal(): Boolean = secure.getBoolean(KEY_PUSH_ENABLED, true)
    fun setEnabledLocal(enabled: Boolean) = secure.putBoolean(KEY_PUSH_ENABLED, enabled)


    /**
     * FCM SDK로부터 새 토큰을 받았을 때 호출
     */
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

    private suspend fun currentOrFetchToken(): String {
        secure.getString(KEY_FCM_TOKEN)?.let { if (it.isNotBlank()) return it }
        secure.getString(KEY_FCM_TOKEN_PENDING)?.let { if (it.isNotBlank()) return it }
        // FCM에서 즉시 토큰 받기
        val fresh = FirebaseMessaging.getInstance().token.await()
        secure.putString(KEY_FCM_TOKEN, fresh)
        return fresh
    }

    private suspend fun syncToServer(token: String) {
        val appVersion = readAppVersionOrNull()
        val enabled = getEnabledLocal()

        val req = RegisterFcmModels.RegisterFcmRequest(
            token = token,
            platform = "android",
            appVersion = appVersion,
            locale = Locale.getDefault().toLanguageTag(),
            deviceId = readAndroidId(),
            installationId = readInstallationsId()
        )

        var attempt = 0
        val maxAttempts = 3
        val baseDelay = 400L

        while (attempt < maxAttempts) {
            attempt++
            try {
                // 헤더(Authorization or X-Guest-Id)는 FcmApi 내부에서 자동 적용
                api.registerFcmToken(req = req,)
                api.setPushEnabled(token, enabled)
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
     * 로그인 완료 혹은 부팅 시(세션 복구 직후)에 호출.
     * - 현재 보유 토큰이 있으면 Authorization 헤더로 재등록(게스트→유저 승격)
     * - 토큰이 없으면 FCM SDK가 곧 콜백(onNewToken)해줄 때까지 대기
     */
    suspend fun onLoginOrBootstrap() {
        val token = secure.getString(KEY_FCM_TOKEN) ?: secure.getString(KEY_FCM_TOKEN_PENDING)
        if (token.isNullOrBlank()) {
            logger.d("PushHandler", "onLoginOrBootstrap: no token yet, wait for FCM callback")
            return
        }
        syncToServer(token)
    }

    /**
     * 로그아웃 시 호출.
     * - 보수적으로는 /unregister-fcm 를 만들어 삭제하는 게 최선이지만,
     * - 간단한 접근: 같은 토큰을 게스트 헤더로 다시 register-fcm 호출 → user_id=null, guest_id=<uuid>로 강등
     */
    suspend fun onLogout() {
        val token = secure.getString(KEY_FCM_TOKEN) ?: return
        logger.d("PushHandler", "onLogout: degrade token to guest")

        val req = RegisterFcmModels.RegisterFcmRequest(
            token = token,
            platform = "android",
            appVersion = readAppVersionOrNull(),
            locale = Locale.getDefault().toLanguageTag(),
            deviceId = readAndroidId(),
            installationId = readInstallationsId()
        )

        runCatching {
            api.registerFcmToken(req) // 현재는 Authorization 없으니 X-Guest-Id 헤더로 저장됨
            api.setPushEnabled(token, getEnabledLocal())
        }.onSuccess {
            logger.d("PushHandler", "logout degrade success")
        }.onFailure {
            logger.e("PushHandler", "logout degrade failed", it)
        }
    }

    /**
     * 스위치 토글 진입점: 권한 OK일 때 호출
     */
    suspend fun togglePushEnabled(enabled: Boolean) {
        // 낙관적 로컬 저장
        setEnabledLocal(enabled)
        // 서버 반영
        val token = currentOrFetchToken()
        api.setPushEnabled(token, enabled)
        // (선택) register-fcm 재호출로 메타 재정합
        runCatching {
            api.registerFcmToken(
                RegisterFcmModels.RegisterFcmRequest(
                    token = token,
                    platform = "android",
                    appVersion = readAppVersionOrNull(),
                    locale = Locale.getDefault().toLanguageTag(),
                    deviceId = readAndroidId(),
                    installationId = readInstallationsId()
                )
            )
        }
    }
}
