package com.handylab.ruto

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.handylab.ruto.data.fcm.RoutinePushHandler
import com.handylab.ruto.domain.auth.AuthState
import com.handylab.ruto.ui.auth.AuthViewModel
import com.handylab.ruto.ui.setting.SettingViewModel
import com.handylab.ruto.ui.setting.ThemeMode
import com.handylab.ruto.ui.theme.RutoTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var supabase: SupabaseClient
    @Inject lateinit var pushHandler: RoutinePushHandler
    private val authViewModel: AuthViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        supabase.handleDeeplinks(intent)

        splash.setKeepOnScreenCondition {
            authViewModel.authState.value is AuthState.Loading
        }

        setContent {
            val themeMode by settingViewModel.themeMode.collectAsStateWithLifecycle()

            val useDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            RutoTheme(darkTheme = useDarkTheme) {
                AppRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        supabase.handleDeeplinks(intent)
    }
}