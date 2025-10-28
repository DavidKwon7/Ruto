package com.example.ruto.ui.setting

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ruto.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    vm: AuthViewModel = hiltViewModel(),
) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("내 정보") })
        }
    ) { pad ->
        Column(
            Modifier
                .padding(paddingValues = pad)
                .padding(horizontal = 12.dp)
        ) {
            WrappedContent(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(fontWeight = FontWeight.Bold, text = "알림")
                        Text("푸시 알림 설정")
                    }
                    var checked = remember { mutableStateOf(false) }
                    Switch(
                        checked = checked.value,
                        onCheckedChange = {
                            checked.value = it
                        }
                    )
                }
            }
            Spacer(Modifier.padding(vertical = 12.dp))

            WrappedContent(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(fontWeight = FontWeight.Bold, text = "앱 버전")
                    Text("v0.0.0")
                }
            }
            Spacer(Modifier.padding(vertical = 12.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.loading,
                onClick = { vm.signOut() }
            ) { Text("로그아웃") }

        }
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
                width = 1.dp, color = Color.LightGray,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(4.dp)
    ) {
        content.invoke()
    }
}