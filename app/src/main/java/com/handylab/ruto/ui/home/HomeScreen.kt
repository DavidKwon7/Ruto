package com.handylab.ruto.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.handylab.ruto.ui.auth.AuthViewModel
import com.handylab.ruto.ui.routine.RoutineCreateScreen
import com.handylab.ruto.ui.routine.RoutineListScreen
import com.handylab.ruto.ui.routine.edit.RoutineEditScreen
import com.handylab.ruto.ui.setting.SettingScreen
import com.handylab.ruto.ui.setting.profile.ProfileEditScreen
import com.handylab.ruto.ui.statistics.StatisticsScreen

@Immutable
data class BottomDest(val route: String, val label: String, val icon: ImageVector)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    vm: AuthViewModel = hiltViewModel()
) {
    val tabsNav = rememberNavController()
    val items = listOf(
        BottomDest("tab/routineList", "루틴", Icons.Outlined.List),
        BottomDest("tab/statistics", "통계", Icons.Outlined.DateRange),
        BottomDest("tab/setting", "설정", Icons.Outlined.Settings),
    )

    val current by tabsNav.currentBackStackEntryAsState()
    val currentRoute = current?.destination?.route

    val ui by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { dest ->
                    val selected = currentRoute == dest.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabsNav.navigate(dest.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(tabsNav.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { pad ->
        if (ui.loading) CircularProgressIndicator()

        NavHost(
            navController = tabsNav,
            startDestination = "tab/routineList",
            modifier = Modifier.padding(pad)
        ) {
            composable("tab/routineList") { RoutineListScreen(tabsNav) }
            composable("tab/statistics") { StatisticsScreen(tabsNav) }
            composable("tab/setting") { SettingScreen(tabsNav) }
            composable("tab/routineCreate") {RoutineCreateScreen(tabsNav)}
            composable(
                route = "tab/routine/edit/{id}",
                arguments = listOf(navArgument("id"){ defaultValue = "" })
            ) {
                RoutineEditScreen(tabsNav)
            }
            composable("tab/profileEdit") { ProfileEditScreen(tabsNav) }
        }
    }
}