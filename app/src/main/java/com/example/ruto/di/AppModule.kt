package com.example.ruto.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.ruto.BuildConfig
import com.example.ruto.auth.AuthProvider
import com.example.ruto.auth.GoogleAuthProvider
import com.example.ruto.auth.KakaoAuthProvider
import com.example.ruto.data.security.EncryptedPrefsSecureStore
import com.example.ruto.data.security.SecureStore
import com.example.ruto.util.AppLogger
import com.example.ruto.util.LogcatLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLogger(): AppLogger = LogcatLogger()

    @Provides
    @Singleton
    fun provideGoogleWebClientId(): String =
        BuildConfig.WEB_CLIENT_ID

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth) {
                scheme = "io.supabase"
                host = "callback"
            }
            install(Postgrest)
        }

    @Provides
    @Singleton
    fun provideSecureStore(
        @ApplicationContext context: Context
    ): SecureStore = EncryptedPrefsSecureStore(context)

    @Provides
    @Singleton
    fun provideAuthProviders(): List<AuthProvider> =
        listOf(
            GoogleAuthProvider(BuildConfig.WEB_CLIENT_ID),
            KakaoAuthProvider()
            // NaverAuthProvider(...) 추가 가능
        )

}