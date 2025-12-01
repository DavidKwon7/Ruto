package com.handylab.ruto.ui.setting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.handylab.ruto.ui.auth.AuthViewModel
import com.handylab.ruto.ui.state.UiState
import com.handylab.ruto.util.getVersionName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    settingViewModel: SettingViewModel = hiltViewModel(),
) {
    val authUi by authViewModel.uiState.collectAsStateWithLifecycle()
    val settingUi by settingViewModel.uiState.collectAsStateWithLifecycle()
    val themeMode by settingViewModel.themeMode.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val grantedNow = remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        grantedNow.value = granted
        settingViewModel.afterPermissionResult(granted)
    }

    LaunchedEffect(settingUi.needsPermission) {
        if (settingUi.needsPermission && Build.VERSION.SDK_INT >= 33) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("내 정보") })
        }
    ) { pad ->
        SettingContent(
            modifier = Modifier
                .padding(paddingValues = pad)
                .padding(horizontal = 12.dp),
            authUi = authUi,
            pushUi = settingUi,
            themeMode = themeMode,
            onPushToggle = { want ->
                val granted = if (Build.VERSION.SDK_INT >= 33)
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                else true

                settingViewModel.onToggleRequest(
                    wantEnabled = want,
                    permissionGranted = granted
                )
            },
            onThemeChange = { mode -> settingViewModel.setThemeMode(mode) },
            onSignOut = { authViewModel.signOut() },
            appVersion = getVersionName()
        )
    }
}

@Composable
private fun SettingContent(
    modifier: Modifier = Modifier,
    authUi: UiState,          // 타입명은 실제 프로젝트에 맞게 수정
    pushUi: PushUiState,
    themeMode: ThemeMode,
    onPushToggle: (Boolean) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onSignOut: () -> Unit,
    appVersion: String,
) {
    Column(modifier = modifier) {
        PushSettingSection(
            ui = pushUi,
            onToggle = onPushToggle
        )

        Spacer(Modifier.padding(vertical = 12.dp))

        ThemeSettingSection(
            themeMode = themeMode,
            onThemeChange = onThemeChange
        )

        Spacer(Modifier.padding(vertical = 12.dp))

        AppVersionSection(appVersion = appVersion)

        Spacer(Modifier.padding(vertical = 12.dp))

        LogoutSection(
            loading = authUi.loading,
            onSignOut = onSignOut
        )
    }
}

// 🔔 푸시 알림 섹션
@Composable
private fun PushSettingSection(
    ui: PushUiState,
    onToggle: (Boolean) -> Unit,
) {
    WrappedContent(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(fontWeight = FontWeight.Bold, text = "알림")
                Text("푸시 알림 설정")
            }
            Switch(
                checked = ui.enabled,
                enabled = !ui.loading,
                onCheckedChange = { want -> onToggle(want) }
            )
        }
    }
}

// 🎨 테마 설정 섹션
@Composable
private fun ThemeSettingSection(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
) {
    WrappedContent(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Text(fontWeight = FontWeight.Bold, text = "테마")
            Text("앱 화면의 밝기 모드를 설정합니다.")
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeChange(ThemeMode.SYSTEM) }
                )
                Text(text = "시스템 설정 따르기")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeChange(ThemeMode.LIGHT) }
                )
                Text(text = "항상 라이트 모드")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { onThemeChange(ThemeMode.DARK) }
                )
                Text(text = "항상 다크 모드")
            }
        }
    }
}

// ℹ️ 앱 버전 섹션
@Composable
private fun AppVersionSection(
    appVersion: String
) {
    WrappedContent(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(fontWeight = FontWeight.Bold, text = "앱 버전")
            Text("v $appVersion")
        }
    }
}

// 🚪 로그아웃 섹션
@Composable
private fun LogoutSection(
    loading: Boolean,
    onSignOut: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = !loading,
        onClick = onSignOut
    ) {
        Text("로그아웃")
    }
}

// 기존 래퍼는 그대로 재사용
@Composable
fun WrappedContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(4.dp)
    ) {
        content()
    }
}
