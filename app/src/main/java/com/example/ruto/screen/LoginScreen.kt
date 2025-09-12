package com.example.ruto.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ruto.auth.AuthProviderFactory

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val providers = remember { AuthProviderFactory.getProviders() }

    Column {
        Text("로그인 화면")

        providers.forEach { provider ->
            Button(
                onClick = {
                    provider.signIn(context) { token, error ->
                        if (token != null) {
                            context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                .edit()
                                .putString("${provider.name}_token", token)
                                .apply()

                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "${provider.name} 로그인 실패: ${error?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text("Sign in with ${provider.name}")
            }
        }
    }
}