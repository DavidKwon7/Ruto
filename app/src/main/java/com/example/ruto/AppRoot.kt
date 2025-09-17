package com.example.ruto

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

@Composable
fun AppRoot(
    vm: AuthViewModel = hiltViewModel()
) {
    val nav = rememberNavController()
    val auth by vm.authState.collectAsStateWithLifecycle()
    val boot by vm.bootstrapDone.collectAsStateWithLifecycle()

    LaunchedEffect(boot, auth) {
        if (!boot) return@LaunchedEffect
        val target = when (auth) {
            is AuthState.SignedOut -> "login"
            is AuthState.SignedIn -> "home"
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

            /*LaunchedEffect(boot) {
                if (boot) {
                    val dest = if (auth is AuthState.SignedIn) "home" else "login"
                    nav.navigate(dest) {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }*/
        }
        composable("login") { LoginScreen(nav) }
        composable("home") { HomeScreen(nav) }
    }
}