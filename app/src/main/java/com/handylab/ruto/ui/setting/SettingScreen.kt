package com.handylab.ruto.ui.setting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.handylab.ruto.auth.isGuest
import com.handylab.ruto.ui.auth.AuthViewModel
import com.handylab.ruto.ui.event.UiEvent
import com.handylab.ruto.ui.state.UiState
import com.handylab.ruto.util.getVersionName
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    settingViewModel: SettingViewModel = hiltViewModel(),
) {
    val authUi by authViewModel.uiState.collectAsStateWithLifecycle()
    val settingUi by settingViewModel.uiState.collectAsStateWithLifecycle()
    val profileUi by settingViewModel.profileUi.collectAsStateWithLifecycle()
    val themeMode by settingViewModel.themeMode.collectAsStateWithLifecycle()

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val isGuest = authState.isGuest

    val context = LocalContext.current
    val grantedNow = remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        grantedNow.value = granted
        settingViewModel.afterPermissionResult(granted)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { settingViewModel.onAvatarSelected(it) }
    }

    LaunchedEffect(settingUi.needsPermission) {
        if (settingUi.needsPermission && Build.VERSION.SDK_INT >= 33) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        settingViewModel.uiEvent.collectLatest { e ->
            if (e is UiEvent.ShowToastMsg)
                Toast.makeText(
                    context, e.message, Toast.LENGTH_SHORT
                ).show()
        }
    }

    // 로그인 유저일 때만 프로필 로드
    LaunchedEffect(isGuest) {
        if (!isGuest) {
            settingViewModel.loadProfile()
        }
    }

    fun requireNonGuest(action: () -> Unit) {
        if (isGuest) {
            Toast.makeText(
                context,
                "게스트 유저는 해당 기능 사용이 불가능합니다.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            action()
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
            profileUi = profileUi,
            isGuest = isGuest,
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
            appVersion = getVersionName(),
            onNicknameChange = { settingViewModel.onNicknameChange(it) },
            onNicknameSave = {
                requireNonGuest {
                    settingViewModel.saveNickname()
                }
            },
            onAvatarClick = {
                requireNonGuest {
                    imagePickerLauncher.launch("image/*")
                }
            }
        )
    }
}

@Composable
private fun SettingContent(
    modifier: Modifier = Modifier,
    authUi: UiState,
    pushUi: PushUiState,
    themeMode: ThemeMode,
    profileUi: ProfileUiState,
    isGuest: Boolean,
    onPushToggle: (Boolean) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onSignOut: () -> Unit,
    appVersion: String,
    onNicknameChange: (String) -> Unit,
    onNicknameSave: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    Column(modifier = modifier
        .verticalScroll(rememberScrollState())) {
        ProfileSection(
            ui = profileUi,
            isGuest = isGuest,
            onNicknameChange = onNicknameChange,
            onNicknameSave = onNicknameSave,
            onAvatarClick = onAvatarClick
        )
        Spacer(Modifier.padding(vertical = 12.dp))

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

@Composable
private fun ProfileSection(
    ui: ProfileUiState,
    isGuest: Boolean,
    onNicknameChange: (String) -> Unit,
    onNicknameSave: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    WrappedContent(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(fontWeight = FontWeight.Bold, text = "프로필")
            Text("닉네임 및 프로필 사진을 관리합니다.")
            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (ui.avatarUrl != null) {
                        AsyncImage(
                            model = ui.avatarUrl,
                            contentDescription = "프로필 이미지",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "기본 프로필",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = ui.nickname,
                        onValueChange = onNicknameChange,
                        label = { Text("닉네임") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = onNicknameSave,
                        enabled = !ui.saving,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (ui.saving) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 6.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text("수정")
                    }

                    if (isGuest) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "게스트 모드에서는 프로필 수정이 불가능합니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (ui.loading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            ui.error?.let { error ->
                Log.e("SettingScreenError",error)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

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
            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeChange(ThemeMode.SYSTEM) }
                )
                Text(text = "시스템 설정")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeChange(ThemeMode.LIGHT) }
                )
                Text(text = "라이트 모드")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { onThemeChange(ThemeMode.DARK) }
                )
                Text(text = "다크 모드")
            }
        }
    }
}

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
