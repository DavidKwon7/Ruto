package com.example.ruto.screen

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ruto.R
import com.example.ruto.auth.AuthProvider
import com.example.ruto.ui.auth.AuthViewModel
import com.example.ruto.ui.event.UiEvent
import com.example.ruto.ui.state.UiState
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    vm: AuthViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val activity = LocalContext.current as? Activity

    LaunchedEffect(vm) {
        vm.events.collectLatest { e ->
            if (e is UiEvent.ShowSnackbar) snackbar.showSnackbar(e.message)
        }
    }

    val providers = remember(vm) { vm.availableProviders() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("로그인") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Box(
            Modifier
            .fillMaxSize()
            .padding(pad)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("계정을 선택해 로그인하세요")
                Spacer(Modifier.height(24.dp))

                providers.forEach { p ->
                    ProviderButton(
                        name = p.name,
                        enabled = !ui.loading && activity != null,
                        onClick = { activity?.let { vm.signIn(it, p) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }

                ui.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (ui.loading) {
                Box(Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)))
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun ProviderButton(
    name: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (label, container, content, border, iconPainter) = when (name.lowercase()) {
        "google" -> ProviderUi(
            label = "Google로 시작하기",
            container = Color.White,
            content = Color(0xFF1F1F1F),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
            icon = painterResource(id = R.drawable.img_google_login) // 벡터/PNG
        )
        "kakao" -> ProviderUi(
            label = "카카오로 시작하기",
            container = Color(0xFFFEE500),
            content = Color(0xFF191600),
            border = null,
            icon = painterResource(id = R.drawable.img_kakao_login)
        )
        else -> ProviderUi(
            label = "Sign in with $name",
            container = MaterialTheme.colorScheme.primary,
            content = MaterialTheme.colorScheme.onPrimary,
            border = null,
            icon = null
        )
    }

    val shape = RoundedCornerShape(12.dp)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = shape,
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = container.copy(alpha = 0.6f),
            disabledContentColor = content.copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (iconPainter != null) {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = Color.Unspecified // 원본 컬러 유지
                )
            }
            Text(text = label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

private data class ProviderUi(
    val label: String,
    val container: Color,
    val content: Color,
    val border: BorderStroke?,
    val icon: Painter?
)