package com.example.ruto.screen

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ruto.auth.AuthProviderFactory
import com.example.ruto.domain.AuthState
import com.example.ruto.ui.auth.AuthViewModel
import com.example.ruto.ui.event.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    vm: AuthViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val auth by vm.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val act = context as Activity

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.events.collect { if (it is UiEvent.ShowSnackbar) snackbar.showSnackbar(it.message) }
    }
    LaunchedEffect(auth) {
        if (auth is AuthState.SignedIn) navController.navigate("home"){
            popUpTo("login") { inclusive = true }
        }
    }

    val providers = remember { vm.availableProviders() }

    Scaffold(
        topBar = { TopAppBar(title = {Text("로그인")}) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            Column(Modifier.fillMaxWidth().align(Alignment.Center).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Text("계정을 선택해 로그인하세요")
                Spacer(Modifier.height(24.dp))

                providers.forEach { p ->
                    Button(
                        enabled = !ui.loading,
                        onClick = { vm.signIn(act, p) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) { Text("Sign in with ${p.name}") }
                }
                ui.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (ui.loading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}