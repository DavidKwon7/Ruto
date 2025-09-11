package com.example.ruto.auth

import android.app.Activity
import android.content.Context
import com.kakao.sdk.user.UserApiClient

class KakaoAuthProvider : AuthProvider {

    override val name: String = "Kakao"

    override fun isSignedIn(context: Context): Boolean {
        return UserApiClient.instance.isKakaoTalkLoginAvailable(context)
    }

    override fun signIn(
        context: Context,
        onResult: (String?, Exception?) -> Unit
    ) {
        val userApiClient = UserApiClient.instance
        if (userApiClient.isKakaoTalkLoginAvailable(context)) {
            userApiClient.loginWithKakaoTalk(context as Activity) { token, error ->
                if (error != null) {
                    // val exception = if (error is Exception) error else Exception(error)
                    val exception = error as? Exception ?: Exception(error)
                    onResult(null, exception)
                } else {
                    onResult(token?.accessToken, null)
                }
            }
        }
    }

    override fun signOut(context: Context) {
        UserApiClient.instance.logout {

        }
    }
}