package com.handylab.ruto.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.handylab.ruto.BuildConfig
import com.handylab.ruto.auth.AuthProvider
import com.handylab.ruto.auth.GoogleAuthProvider
import com.handylab.ruto.auth.GuestAuthProvider
import com.handylab.ruto.auth.KakaoAuthProvider
import com.handylab.ruto.data.local.RoutineCompletionDao
import com.handylab.ruto.data.local.routine.RoutineDao
import com.handylab.ruto.data.security.EncryptedPrefsSecureStore
import com.handylab.ruto.data.security.SecureStore
import com.handylab.ruto.data.statistics.LiveMonthlyStatsCalculator
import com.handylab.ruto.util.AppLogger
import com.handylab.ruto.util.LogcatLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.ExternalAuthAction
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

                // 외부 브라우저 대신 Custom Tabs 선호
                defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
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
    fun provideAuthProviders(
        supabase: SupabaseClient
    ): List<AuthProvider> =
        listOf(
            GoogleAuthProvider(BuildConfig.WEB_CLIENT_ID),
            KakaoAuthProvider(supabase),
            // NaverAuthProvider(...) 추가 가능
            GuestAuthProvider()
        )

    /**
     * LiveMonthlyStatsCalculator는 @Inject 생성자이므로 별도 @Provides 없이도 주입 가능하지만,
     * 의존성 명시와 테스트 대체 용이성을 위해 명시 제공.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideLiveMonthlyStatsCalculator(
        routineDao: RoutineDao,
        completionDao: RoutineCompletionDao,
        supabase: SupabaseClient,
        secure: SecureStore
    ): LiveMonthlyStatsCalculator =
        LiveMonthlyStatsCalculator(routineDao, completionDao, supabase, secure)

}