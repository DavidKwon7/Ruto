package com.example.ruto.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ruto.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    vm: AuthViewModel = hiltViewModel()
    ) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("메인") }) }) { pad ->
    Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉 메인 화면")
            Spacer(Modifier.height(12.dp))
            Button(enabled = !ui.loading, onClick = { vm.signOut() }) { Text("로그아웃") }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { navController.navigate("routineCreate") }) { Text("루틴 생성") }
            Button(onClick = { navController.navigate("routineList") }) { Text("루틴 리스트") }

        }
        if (ui.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
    }}
}
