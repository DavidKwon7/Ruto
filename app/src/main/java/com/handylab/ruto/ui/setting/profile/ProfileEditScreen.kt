package com.handylab.ruto.ui.setting.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.handylab.ruto.auth.userIdOrNull
import com.handylab.ruto.ui.auth.AuthViewModel
import com.handylab.ruto.ui.setting.ProfileUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: ProfileEditViewModel = hiltViewModel(),
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    val userId = authState.userIdOrNull
    val avatarCacheKey = remember(userId, ui.avatarVersion) {
        userId?.let { "avatar_${it}_v${ui.avatarVersion}" }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onAvatarSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프로필 수정") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.onSave()
                        },
                        enabled = !ui.saving && !ui.loading
                    ) {
                        Text("저장")
                    }
                }
            )
        }
    ) { pad ->
        ProfileEditContent(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            ui = ui,
            avatarCacheKey = avatarCacheKey,
            onAvatarClick = { imagePickerLauncher.launch("image/*") },
            onNicknameChange = viewModel::onNicknameChange,
            onClickSave = {
                viewModel.onSave()
                // navController.popBackStack()
            }
        )
    }
}

@Composable
private fun ProfileEditContent(
    modifier: Modifier = Modifier,
    ui: ProfileUiState,
    avatarCacheKey: String?,
    onAvatarClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onClickSave: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))


        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onAvatarClick)
        ) {
            AvatarImage(
                avatarUrl = ui.avatarUrl,
                cacheKey = avatarCacheKey, // 서버 이미지일 때만 캐시 키 사용됨
                size = 200.dp
            )
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = ui.nickname,
            onValueChange = onNicknameChange,
            label = { Text("닉네임") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onClickSave,
            enabled = !ui.saving && !ui.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (ui.saving) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
            }
            Text("저장하기")
        }

        if (ui.loading) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        ui.error?.let { error ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}