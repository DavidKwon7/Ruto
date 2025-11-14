package com.handylab.ruto.util

import com.handylab.ruto.data.security.SecureStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import java.util.UUID

private const val KEY_GUEST_ID = "guest_id"

/**
 * 게스트 ID를 항상 보장
 */
fun ensureGuestId(secure: SecureStore): String {
    return secure.getString(KEY_GUEST_ID) ?: UUID.randomUUID().toString().also {
        secure.putString(KEY_GUEST_ID, it)
    }
}

/**
 * 로그인 상태면 Authorization 헤더만, 아니면 X-Guest-Id 헤더만 추가
 */
suspend fun HttpRequestBuilder.applyAuthHeaders(
    supabase: SupabaseClient,
    secure: SecureStore
) {
    val access = supabase.auth.currentSessionOrNull()?.accessToken

    if (access != null) {
        header(HttpHeaders.Authorization, "Bearer $access")
    } else {
        val gid = ensureGuestId(secure)
        header("X-Guest-Id", gid)
    }
}