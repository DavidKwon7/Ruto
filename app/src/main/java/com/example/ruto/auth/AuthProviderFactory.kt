package com.example.ruto.auth

import com.example.ruto.BuildConfig

object AuthProviderFactory {
    fun getProviders(): List<AuthProvider> {
        return listOf(
            // GoogleAuthProvider("602387918082-rfct3i0kpncv8p3jr5nsjchp16u9ac7v.apps.googleusercontent.com"),
            GoogleAuthProvider(BuildConfig.WEB_CLIENT_ID),
            // TODO 네이버 인증
            // KakaoAuthProvider()
            )
    }
}