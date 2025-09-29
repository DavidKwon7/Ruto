package com.example.ruto.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun EnsureNotificationPermission() {
    if (Build.VERSION.SDK_INT < 33) return

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    var showRationale by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                // 사용자가 “다시 묻지 않기”까지 누르면 shouldShowRequestPermissionRationale = false 이고, 설정으로 유도
                val shouldRationale =
                    activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                if (shouldRationale) showRationale = true else showSettingsDialog = true
            }
        }
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 간단한 설명(권한 이유) 다이얼로그
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text("허용") }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text("취소") }
            },
            title = { Text("알림 권한 필요") },
            text = { Text("루틴 알림을 받으려면 알림 권한이 필요합니다.") }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${activity.packageName}")
                    )
                    activity.startActivity(intent)
                }) { Text("설정으로 이동") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("취소") }
            },
            title = { Text("알림 권한이 비활성화됨") },
            text = { Text("설정 > 알림에서 권한을 허용해 주세요.") }
        )
    }
}