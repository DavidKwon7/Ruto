package com.example.ruto.ui.setting

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ruto.ui.auth.AuthViewModel

@Composable
fun SettingScreen(
    navController: NavHostController,
    vm: AuthViewModel = hiltViewModel(),
) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    Text("환경설정")
    Button(enabled = !ui.loading, onClick = { vm.signOut() }) { Text("로그아웃") }
}