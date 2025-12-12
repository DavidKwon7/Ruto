package com.handylab.ruto.auth

import android.app.Activity
import android.os.Build
import android.util.Log
import com.handylab.ruto.domain.auth.IdTokenPayload
import kotlin.coroutines.cancellation.CancellationException

class GoogleAuthProvider(
    private val webClientId: String
) : AuthProvider {
    override val name: String = "Google"

    private val cmStrategy by lazy { CredentialManagerStrategy(webClientId) }
    private val legacyStrategy by lazy { LegacyGoogleSignInStrategy(webClientId) }

    override suspend fun acquireIdToken(activity: Activity): IdTokenPayload {
        // 33 미만 기기는 legacyStrategy
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return legacyStrategy.acquireIdToken(activity)
        }

        // 33 이상은 cmStrategy -> 실패 시 legacyStrategy 폴백
        return try {
            cmStrategy.acquireIdToken(activity)
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            // 자격 증명 없음: 계정이 없거나, CM이 계정을 안 내주는 케이스
            Log.w("GoogleAuth", "No credentials via CM. Fallback to Legacy.", e)
            legacyStrategy.acquireIdToken(activity)
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            // 사용자가 명시적으로 취소한 경우에 폴백 없이 그대로 취소 전달
            Log.i("GoogleAuth", "User cancelled Google sign-in via CM.", e)
            throw CancellationException("User cancelled Google sign-in", e)
        } catch (e: androidx.credentials.exceptions.GetCredentialException) {
            // 그 외 CM 관련 에러 (네트워크/Play 서비스 이슈 등) -> Legacy 폴백
            Log.e("GoogleAuth", "CM error (${e::class.simpleName}): ${e.message}. Fallback to Legacy.", e)
            legacyStrategy.acquireIdToken(activity)
        }
    }
}