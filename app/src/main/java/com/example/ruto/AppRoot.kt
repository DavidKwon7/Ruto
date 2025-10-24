package com.example.ruto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ruto.domain.AuthState
import com.example.ruto.screen.HomeScreen
import com.example.ruto.screen.LoginScreen
import com.example.ruto.ui.auth.AuthViewModel
import com.example.ruto.ui.permission.EnsureNotificationPermission

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppRoot(
    vm: AuthViewModel = hiltViewModel()
) {
    val nav = rememberNavController()
    val auth by vm.authState.collectAsStateWithLifecycle()
    val boot by vm.bootstrapDone.collectAsStateWithLifecycle()

    EnsureNotificationPermission()

    LaunchedEffect(boot, auth) {
        if (!boot) return@LaunchedEffect
        val target = when (auth) {
            is AuthState.SignedIn, AuthState.Guest  -> "home"
            is AuthState.SignedOut, is AuthState.Error -> "login"
            else -> return@LaunchedEffect
        }
        val current = nav.currentBackStackEntry?.destination?.route
        if (current != target) nav.navigate(target) {
            popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
            launchSingleTop = true
        }
    }
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        composable("login") { LoginScreen(nav) }
        composable("home") { HomeScreen(nav) }
    }
}