package com.example.ruto.ui.routine

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.ruto.domain.routine.RoutineRead
import com.example.ruto.ui.util.bounceClick

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutineListScreen(
    navController: NavHostController,
    vm: RoutineListViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    /*LaunchedEffect(ui.items) {
        vm.refresh()
    }*/

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 루틴") },
                actions = { IconButton(onClick = { vm.refresh() }) { Icon(Icons.Default.Refresh, null) } }

            )
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                ui.loading -> CircularProgressIndicator(Modifier.padding(24.dp))
                ui.error != null -> Text("에러: ${ui.error}", modifier = Modifier.padding(24.dp))
                else -> RoutineList(ui.items) { r ->
                    navController.navigate("routine/edit/${r.id}")
                }
            }
        }
    }
}

@Composable
private fun RoutineList(items: List<RoutineRead>, onClick: (RoutineRead) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp)) {
        items(items) { r ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    // .clickable { onClick(r) }
                    .bounceClick { onClick(r) }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(r.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("${r.cadence} | ${r.startDate} ~ ${r.endDate}")
                    if (r.notifyEnabled) {
                        Text("알림: ${r.notifyTime ?: "-"} (${r.timezone})", style = MaterialTheme.typography.bodySmall)
                    }
                    if (r.tags.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("태그: ${r.tags.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}