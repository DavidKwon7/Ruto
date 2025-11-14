package com.handylab.ruto.ui.routine

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.handylab.ruto.ui.util.bounceClick
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutineListScreen(
    navController: NavHostController,
    vm: RoutineListViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 루틴") },
                actions = { IconButton(onClick = {
                    scope.launch {
                    vm.refresh() }
                }) { Icon(Icons.Default.Refresh, "루틴 수정") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                contentColor = Color.White,
                containerColor = Color.Gray,
                shape = CircleShape,
                onClick = {
                    navController.navigate("tab/routineCreate")
                }
            ) {
                Icon(Icons.Default.Add, "루틴 생성")
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                ui.loading -> CircularProgressIndicator(Modifier.padding(24.dp))
                ui.error != null -> Text("에러: ${ui.error}", modifier = Modifier.padding(24.dp))
                else -> RoutineList(
                    items = ui.items,
                    onClick = { r -> navController.navigate("tab/routine/edit/${r.routine.id}") },
                    onToggleComplete = { r -> vm.toggleComplete(r) }
                )
            }
        }
    }
}

@Composable
private fun RoutineList(
    items: List<Item>,
    onClick: (Item) -> Unit,
    onToggleComplete: (Item) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        items(items, key = {it.routine.id}) { item ->
            val r = item.routine
            val completed = item.completedToday

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    // .clickable { onClick(r) }
                    .bounceClick (
                        onClick = { onToggleComplete(item) }
                    )
            ) {
                Row(
                    Modifier
                        .background(if (completed) Color(0xFF98FB98) else Color(0xFFD3D3D3))
                        .padding(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Row {
                            Text(r.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.padding(horizontal = 2.dp))
                            if (completed) Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "루틴 완료")
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${r.cadence} | ${r.startDate} ~ ${r.endDate}")
                        if (r.notifyEnabled) {
                            Text(
                                "알림: ${r.notifyTime ?: "-"} (${r.timezone})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (r.tags.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text("태그: ${r.tags.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    IconButton(
                        onClick = { onClick(item) }
                    ) {
                        Icon(Icons.Default.Settings, "루틴 수정")
                    }
                }
            }
        }
    }
}