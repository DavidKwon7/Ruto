package com.example.ruto.data.routine

import com.example.ruto.BuildConfig
import com.example.ruto.domain.routine.RoutineCreateRequest
import com.example.ruto.domain.routine.RoutineCreateResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineApi @Inject constructor(
    private val client: HttpClient,
    private val supabase: SupabaseClient
) {
    // private val base = BuildConfig.SUPABASE_URL
    private val base = "https://wyqbynrmzndxuiahhdxg.supabase.co/functions/v1/create-routine"

    suspend fun createRoutine(req: RoutineCreateRequest, guestId: String?): RoutineCreateResponse {
        val accessToken = supabase.auth.currentSessionOrNull()?.accessToken
        // return client.post("$base/create-routine") {
        return client.post(base) {
            setBody(req)
            header("apikey", BuildConfig.SUPABASE_KEY)
            accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            guestId?.let { header("X-Guest-Id", it) }
        }.body()
    }
}
